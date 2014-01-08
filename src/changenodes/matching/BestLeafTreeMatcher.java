package changenodes.matching;

/* 
 * Taken and adapted from changedistiller
 * Original code can be retrieved from bitbucket.org/sealuzh/tools-changedistiller
 * No major conceptual changes were introduced
 * Code was adapted so it works directly on JDT nodes instead of the intermediate format
 *
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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;

import changenodes.comparing.DepthFirstNodeIterator;

public class BestLeafTreeMatcher {

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
	   
   }
		   
   
    public void enableDynamicThreshold(int depth, double threshold) {
        fDynamicDepth = depth;
        fDynamicThreshold = threshold;
        fDynamicEnabled = true;
    }

    public void disableDynamicThreshold() {
        fDynamicEnabled = false;
    }

    public void setMatching(Map<ASTNode, ASTNode> leftMatching, Map<ASTNode, ASTNode> rightMatching) {
        this.leftMatching = leftMatching;
        this.rightMatching = rightMatching;
        fNodeSimilarityCalculator.setLeftMatching(leftMatching);
        fNodeSimilarityCalculator.setRightMatching(rightMatching);
    }

    public void match(ASTNode left, ASTNode right) {
        List<LeafPair> matchedLeafs = matchLeaves(left, right);
        // sort matching set according to similarity in descending order
        Collections.sort(matchedLeafs);
        markMatchedLeaves(matchedLeafs);
        matchNodes(left, right);
    }

  
    private void matchNodes(ASTNode left, ASTNode right) {
        for (Iterator<ASTNode> iterator = new DepthFirstNodeIterator(left); iterator.hasNext();) {
			ASTNode x =  iterator.next();
			if(! leftMatching.containsKey(x) && (! NodeClassifier.isLeafStatement(x) || NodeClassifier.isRoot(x))){
		        for (Iterator<ASTNode> rightIterator = new DepthFirstNodeIterator(right); iterator.hasNext();) {
		        	ASTNode y = rightIterator.next();
		        	if( (!rightMatching.containsKey(y)
		        			&& (! NodeClassifier.isLeafStatement(y) || NodeClassifier.isRoot(y))) 
		        			&&	equal(x, y)){
		        		leftMatching.put(x, y);
		        		rightMatching.put(y, x);
		        	}
		        }
			}
        }
    }

    private void markMatchedLeaves(List<LeafPair> matchedLeafs) {
        for (LeafPair pair : matchedLeafs) {
            ASTNode x = pair.getLeft();
            ASTNode y = pair.getRight();
            if(!(leftMatching.containsKey(x) || rightMatching.containsKey(y))){
            	leftMatching.put(x, y);
            	rightMatching.put(y, x);
            }
        }
    }

    private List<LeafPair> matchLeaves(ASTNode left, ASTNode right) {
        List<LeafPair> matchedLeafs = new ArrayList<LeafPair>();
        for (Iterator<ASTNode> iterator = new DepthFirstNodeIterator(left); iterator.hasNext();) {
			ASTNode x =  iterator.next();
			if(NodeClassifier.isLeafStatement(x)){
				for(Iterator<ASTNode> rightIterator = new DepthFirstNodeIterator(right); rightIterator.hasNext();) {
					ASTNode y = rightIterator.next();
					if (NodeClassifier.isLeafStatement(y) && x.getNodeType() == y.getNodeType()){
						double similarity = 0;

                        if (NodeClassifier.isComment(x)) {
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

  
    private boolean equal(ASTNode x, ASTNode y) {
        // inner nodes
        if (areInnerOrRootNodes(x, y) && x.getNodeType() == y.getNodeType()) {
            // little heuristic
            if (NodeClassifier.isRoot(x)) {
                return true;
            } else {
                double t = fNodeSimilarityThreshold;
                
                double simNode = fNodeSimilarityCalculator.calculateSimilarity(x, y);
                double simString = fNodeStringSimilarityCalculator.calculateSimilarity(x.toString(), y.toString());
                if ((simString < fNodeStringSimilarityThreshold) && (simNode >= WEIGHTING_THRESHOLD)) {
                    return true;
                } else {
                    return (simNode >= t) && (simString >= fNodeStringSimilarityThreshold);
                }
            }
        }
        return false;
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