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

import java.util.Iterator;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;

import changenodes.comparing.DepthFirstNodeIterator;

public class ChawatheCalculator implements NodeSimilarityCalculator {
	
	private Map<ASTNode, ASTNode> leftMatching;
	private Map<ASTNode, ASTNode> rightMatching;

    @Override
    public void setLeftMatching(Map<ASTNode, ASTNode> lMatching){
    	leftMatching = lMatching;
    }
    
    @Override
    public void setRightMatching(Map<ASTNode, ASTNode> rMatching){
    	rightMatching = rMatching;
    }

    @Override
    public double calculateSimilarity(ASTNode left, ASTNode right) {
        int common = 0;
        // common(x, y) = {(w, z) in M | x contains w, and y contains z}
        // |common|
        
        for (Map.Entry<ASTNode, ASTNode> entry : leftMatching.entrySet()) {
        	ASTNode l = entry.getKey();
        	ASTNode r = entry.getValue();
        	
            if (isDescendant(left, l) && isDescendant(right, r)) {
                common++;
            }
        }
        int max = maxLeafStatements(left, right);
        return (double) common / (double) max;
    }

    private int maxLeafStatements(ASTNode left, ASTNode right) {
        int leftLeafStatements = getLeafCount(left) - numberOfCommentNodes(left);
        int rightLeafStatements = getLeafCount(right) - numberOfCommentNodes(right);
        return Math.max(leftLeafStatements, rightLeafStatements);
    }
    
    private boolean isDescendant(ASTNode parent, ASTNode child){
    	if(child == null){
    		return false;
    	}
    	ASTNode childParent = child.getParent();
    	return childParent.equals(parent) || isDescendant(parent, childParent);
    }
   
    
    private int getLeafCount(ASTNode node){
    	int count = 0;
    	if(NodeClassifier.isLeafStatement(node)){
    		return 1;
    	}
    	for (Iterator<ASTNode> iterator = new DepthFirstNodeIterator(node); iterator.hasNext();) {
			ASTNode n = iterator.next();
			if(NodeClassifier.isLeafStatement(n)){
				count++;
			}
		}
    	return count;
    }
    
    private int numberOfCommentNodes(ASTNode node){
    	int count = 0;
    	if(isCommentNode(node)){
    		return 1;
    	}
    	for (Iterator<ASTNode> iterator = new DepthFirstNodeIterator(node); iterator.hasNext();) {
			ASTNode n = iterator.next();
			if(isCommentNode(n)){
				count++;
			}
		}
    	return count;
    }

    private boolean isCommentNode(ASTNode node){
    	int type = node.getNodeType();
    	switch(type){
    	case ASTNode.BLOCK_COMMENT:
    	case ASTNode.JAVADOC:
    	case ASTNode.TAG_ELEMENT:
    	case ASTNode.LINE_COMMENT:
    	case ASTNode.METHOD_REF:
    	case ASTNode.METHOD_REF_PARAMETER:
    	case ASTNode.TEXT_ELEMENT:
    		return true;	
    	default:
    		return false;
    	}
    }
}