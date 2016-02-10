package changenodes.matching;

/* 
 * Taken and adapted from changedistiller
 * Original code can be retrieved from bitbucket.org/sealuzh/tools-changedistiller
 * No major conceptual changes were introduced
 * Code was adapted so it works directly on JDT nodes instead of the intermediate format
 * Note that the code no longer works for comments, which are not present in JDT.
 */

/*
 * #%L
 * ChangeDistiller
 * %%
 * Copyright (C) 2011 - 2013 Software Architecture and Evolution Lab, Department of Informatics, UZH
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

import changenodes.comparing.BreadthFirstNodeIterator;
import changenodes.comparing.DepthFirstNodeIterator;
import changenodes.matching.calculators.ChawatheCalculator;
import changenodes.matching.calculators.NGramsCalculator;
import changenodes.matching.calculators.NodeSimilarityCalculator;
import changenodes.matching.calculators.StringSimilarityCalculator;
import changenodes.matching.calculators.TokenBasedCalculator;

public class BestLeafTreeMatcher implements IMatcher {

    private StringSimilarityCalculator fLeafGenericStringSimilarityCalculator;
    private double fLeafGenericStringSimilarityThreshold;

    // Hardcoded! Needs integration into benchmark facilities.
    private StringSimilarityCalculator fLeafCommentStringSimilarityCalculator = new TokenBasedCalculator();
    private static final double LEAF_COMMENT_STRING_SIMILARITY_THRESHOLD = 0.4;

    private NodeSimilarityCalculator fNodeSimilarityCalculator;
    private double fNodeSimilarityThreshold;

    private StringSimilarityCalculator fNodeStringSimilarityCalculator;
    private double fNodeStringSimilarityThreshold;
    private static final double WEIGHTING_THRESHOLD = 0.8;

    private boolean fDynamicEnabled;
    private int fDynamicDepth;
    private double fDynamicThreshold;

    private Map<ASTNode, ASTNode> leftMatching;
    private Map<ASTNode, ASTNode> rightMatching;

   
   public BestLeafTreeMatcher(){
	   this(
			   new NGramsCalculator(2),
			   0.6,
			   new NGramsCalculator(2),
			   0.0,
			   new ChawatheCalculator(),
			   0.4);
			   
   }
   
   public BestLeafTreeMatcher(
		   StringSimilarityCalculator leafStringSimCalc, 
		   double leafStringSimThreshold,
           StringSimilarityCalculator nodeStringSimCalc,
           double nodeStringSimThreshold,
           NodeSimilarityCalculator nodeSimCalc,
           double nodeSimThreshold) {
	   fLeafGenericStringSimilarityCalculator = leafStringSimCalc;
       fLeafGenericStringSimilarityThreshold = leafStringSimThreshold;
       fNodeStringSimilarityCalculator = leafStringSimCalc;
       fNodeStringSimilarityThreshold = leafStringSimThreshold;
       fNodeSimilarityCalculator = nodeSimCalc;
       fNodeSimilarityThreshold = nodeSimThreshold;
       this.leftMatching = new HashMap<ASTNode, ASTNode>();
       this.rightMatching = new HashMap<ASTNode, ASTNode>();
       nodeSimCalc.setLeftMatching(leftMatching);
       nodeSimCalc.setRightMatching(rightMatching);

   }
		   
   
    public void enableDynamicThreshold(int depth, double threshold) {
        fDynamicDepth = depth;
        fDynamicThreshold = threshold;
        fDynamicEnabled = true;
    }

    public void disableDynamicThreshold() {
        fDynamicEnabled = false;
    }

    public Map<ASTNode, ASTNode> getLeftMatching(){
    	return this.leftMatching;
    }
    
    public Map<ASTNode, ASTNode> getRightMatching(){
    	return this.rightMatching;
    }

    public void match(ASTNode left, ASTNode right) throws MatchingException {
        List<LeafPair> matchedLeafs = matchLeaves(left, right);
        // sort matching set according to similarity in descending order
        Collections.sort(matchedLeafs);
        matchIdenticalMethods(left, right);
        markMatchedLeaves(matchedLeafs);
        matchNodes(left, right);
        //both roots need to match
        //we should introduce a fake node
        if(left.getNodeType() != right.getNodeType()){
        	throw new UnmatchingRootsException(left, right);
        }
        leftMatching.put(left,right);
        rightMatching.put(right, left);
    }

  
    private void matchNodes(ASTNode left, ASTNode right) {
        for (Iterator<ASTNode> iterator = new BreadthFirstNodeIterator(left); iterator.hasNext();) {
			ASTNode x =  iterator.next();
			Map<ASTNode, Double> candidateMatches = new HashMap<ASTNode, Double>();
			ASTNode bestMatch = null;
			double bestSimilarity = 0.0;
			if(!leftMatching.containsKey(x) && (! NodeClassifier.isLeafStatement(x) || NodeClassifier.isRoot(x))){
		        for (Iterator<ASTNode> rightIterator = new BreadthFirstNodeIterator(right); rightIterator.hasNext();) {
		        	ASTNode y = rightIterator.next();
		        	if( (!rightMatching.containsKey(y)
		        			&& (! NodeClassifier.isLeafStatement(y) || NodeClassifier.isRoot(y)))){
		        		double stringSimilarity = equal(x, y);
		        		if(stringSimilarity >= fNodeStringSimilarityThreshold && stringSimilarity >= bestSimilarity){
			        		candidateMatches.put(y, stringSimilarity);
		        			bestMatch = y;
		        			bestSimilarity = stringSimilarity;
		        		}
		        	}
		        }
		        //we have found the best node, lets now match them together
		        //if multiple nodes have same similarity we try to find the node whose parents
		        //match.
		        if(bestMatch != null){
		        	Collection<ASTNode> matches = new ArrayList<ASTNode>();
		        	for(Entry<ASTNode, Double> candidatePair : candidateMatches.entrySet() ){
		        		ASTNode candidate = candidatePair.getKey();
		        		double similarity = candidatePair.getValue();
		        		if(similarity == bestSimilarity){
		        			matches.add(candidate);
		        		}
		        	}
		        	for(ASTNode n : matches){
		        		ASTNode parent = n.getParent();
		        		if(parent != null){
		        			ASTNode matchedParent = rightMatching.get(parent);
		        			if(matchedParent != null && x.getParent().equals(matchedParent) && bestMatch != n){
		        				//we have a match whose parents are already matched so this should be the best one
		        				bestMatch = n;
		        			}
		        		}
		        	}
		        	leftMatching.put(x, bestMatch);
		        	rightMatching.put(bestMatch, x);
		        	//special handling of MethodDeclarations, can probably be done cleaner
	        		//note that this is not in the paper, but based on some example runs
	        		if(x instanceof BodyDeclaration){
	        			markBodyDeclaration(x, bestMatch);
	        		}
		        }
			}
        }
    }
    

    private void matchIdenticalMethods(ASTNode left, ASTNode right) {
    	//fix in which some methods got pretty bad matches
    	//feels pretty dirty though (but so does this whole file)
    	for (Iterator<ASTNode> iterator = new BreadthFirstNodeIterator(left); iterator.hasNext();) {
			ASTNode n =  iterator.next();
			ASTNode bestMatch = null;
			if(n.getNodeType() == ASTNode.METHOD_DECLARATION){
				MethodDeclaration x = (MethodDeclaration) n;
				if(!leftMatching.containsKey(x)){
					for (Iterator<ASTNode> rightIterator = new BreadthFirstNodeIterator(right); rightIterator.hasNext();) {
						ASTNode y = rightIterator.next();
						if(y.getNodeType() == x.getNodeType() && 
								x.subtreeMatch(new ASTMatcher(), y)){
							bestMatch = y;
							break;
						}
					}
				}
			    //we have found the best node, lets now match them together
		        //afaik this is also not in the original paper, but we sometimes got some weird matches
		        if(bestMatch != null){
		        	markMatchedNode(x,bestMatch);
		        }
			}
        }
    }

    private void markMatchedLeaves(List<LeafPair> matchedLeafs) {
        for (LeafPair pair : matchedLeafs) {
            ASTNode x = pair.getLeft();
            ASTNode y = pair.getRight();
            markMatchedNode(x, y);
        }
    }
    
    private void markBodyDeclaration(ASTNode x, ASTNode y){
    	BodyDeclaration leftMethod = (BodyDeclaration) x;
    	BodyDeclaration rightMethod = (BodyDeclaration) y;
    	List<IExtendedModifier> leftModifiers = (List<IExtendedModifier>) leftMethod.modifiers();
    	List<IExtendedModifier> rightModifiers = (List<IExtendedModifier>) rightMethod.modifiers();
    	for(IExtendedModifier lm : leftModifiers){
    		if(lm.isModifier()){
    			for(IExtendedModifier rm : rightModifiers){
    				if(rm.isModifier()){
    					Modifier clm = (Modifier) lm;
    					Modifier crm = (Modifier) rm;
    					if(crm.getKeyword().equals(clm.getKeyword())){
        					leftMatching.put(clm, crm);
        					rightMatching.put(crm, clm);
    					}
    				}
    			}
    		}
    	}
    }
    
    //2 nodes match so we match them and all of their children
    @SuppressWarnings("unchecked")
	private void markMatchedNode(ASTNode left, ASTNode right){
    	if(left.getNodeType() != right.getNodeType()){
    		return;
    	}
    	if(!(leftMatching.containsKey(left) || rightMatching.containsKey(right))){
    		leftMatching.put(left, right);
    		rightMatching.put(right, left);
    		List<StructuralPropertyDescriptor> props = (List<StructuralPropertyDescriptor>) left.structuralPropertiesForType();
    		for(StructuralPropertyDescriptor prop : props){
    			if(prop.isChildProperty()){
    				ASTNode newLeft, newRight;
    				newLeft = (ASTNode) left.getStructuralProperty(prop);
    				newRight = (ASTNode) right.getStructuralProperty(prop);
    				if(newLeft != null && newRight != null){
    					markMatchedNode(newLeft, newRight);
    				}
    			} else if(prop.isChildListProperty()){
    				List<ASTNode> lefts, rights;
    				lefts = (List<ASTNode>) left.getStructuralProperty(prop);
    				rights = (List<ASTNode>) right.getStructuralProperty(prop);
    				int times = lefts.size();
    				if(lefts.size() > rights.size()){
    					times = rights.size();
    				}
    				Iterator<ASTNode> leftIt = lefts.iterator();
    				Iterator<ASTNode> rightIt = rights.iterator();
    				for(int i = 0; i < times; ++i){
    					ASTNode rNode = rightIt.next();
						ASTNode lNode = leftIt.next();
						if(lNode != null && rNode != null){
							markMatchedNode(lNode, rNode);
						}
    				}
    			}
    			//we dont handle simple props as they point to objects
    		}
    	}
    }

    private List<LeafPair> matchLeaves(ASTNode left, ASTNode right) {
        List<LeafPair> matchedLeafs = new ArrayList<LeafPair>();
        for (Iterator<ASTNode> iterator = new BreadthFirstNodeIterator(left); iterator.hasNext();) {
			ASTNode x =  iterator.next();
			if(NodeClassifier.isLeafStatement(x)){
				for(Iterator<ASTNode> rightIterator = new BreadthFirstNodeIterator(right); rightIterator.hasNext();) {
					ASTNode y = rightIterator.next();
					if (NodeClassifier.isLeafStatement(y) && x.getNodeType() == y.getNodeType()){
						double similarity = 0;

                        if (NodeClassifier.isInsideComment(x)) {
                            //ignore comments as they are not easily available
                        } else { // ...other statements.
                            similarity =
                                    fLeafGenericStringSimilarityCalculator.calculateSimilarity(
                                            x.toString(),
                                            y.toString());

                            // Important! Otherwise nodes that match poorly will make it into final matching set,
                            // if no better matches are found!
                            if (similarity >= fLeafGenericStringSimilarityThreshold) {
                                matchedLeafs.add(new LeafPair(x, y, similarity));
                            }
                        }
					}
				}
			}
		}
        return matchedLeafs;
    }

  
    private double equal(ASTNode x, ASTNode y) {
        // inner nodes
        if (areInnerOrRootNodes(x, y) && x.getNodeType() == y.getNodeType()) {
            // little heuristic
            if (NodeClassifier.isRoot(x)) {
                return 1.0;
            } else {
                double t = fNodeSimilarityThreshold;
                
                double simNode = fNodeSimilarityCalculator.calculateSimilarity(x, y);
                double simString = fNodeStringSimilarityCalculator.calculateSimilarity(x.toString(), y.toString());
                if ((simString < fNodeStringSimilarityThreshold) && (simNode >= WEIGHTING_THRESHOLD)) {
                    return simString;
                } else {
                    if((simNode >= t) && (simString >= fNodeStringSimilarityThreshold)){
                    	return simString;
                    }
                }
            }
        }
        return 0;
    }

    private boolean areInnerOrRootNodes(ASTNode x, ASTNode y) {
        return areInnerNodes(x, y) || areRootNodes(x, y);
    }

    private boolean areInnerNodes(ASTNode x, ASTNode y) {
    	return !NodeClassifier.isLeafStatement(x) && !NodeClassifier.isLeafStatement(y);
    }

    private boolean areRootNodes(ASTNode x, ASTNode y) {
    	return NodeClassifier.isRoot(x) & NodeClassifier.isRoot(y);
    }
}
