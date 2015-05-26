package changenodes.operations;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ChildPropertyDescriptor;
import org.eclipse.jdt.core.dom.SimplePropertyDescriptor;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

public class Move extends Operation implements IOperation {

	ASTNode original;
	ASTNode newParent;
	ASTNode leftNode;
	ASTNode rightNode;
	ASTNode leftPrimeParent;
	StructuralPropertyDescriptor property;
	int index;
	
	public Move(ASTNode original, ASTNode node, ASTNode newParent, ASTNode rightNode, StructuralPropertyDescriptor property, int index){
		this.original = original;
		this.newParent = newParent;
		this.leftNode = node;
		this.property = property;
		this.index = index;
		this.rightNode = rightNode;
		this.leftPrimeParent = node.getParent();
	}

	public ASTNode getOriginal() {
		return original;
	}
	
	public ASTNode getNewParent() {
		return newParent;
	}

	public ASTNode getLeftNode() {
		return leftNode;
	}
	
	public ASTNode getAffectedNode(){
		return leftNode;
	}
	
	public ASTNode getRightNode(){
		return rightNode;
	}
	
	public ASTNode getLeftPrimeParent(){
		return leftPrimeParent;
	}
	
	public Move setAffectedNode(ASTNode node){
		return new Move(original, node, newParent, rightNode, property, index);
	}

	public StructuralPropertyDescriptor getProperty() {
		return property;
	}

	public int getIndex() {
		return index;
	}
	
	public ASTNode apply(Map<ASTNode, ASTNode> leftMatching, Map<ASTNode, ASTNode> rightMatching){
		boolean unparent = true;
		StructuralPropertyDescriptor prop = leftNode.getLocationInParent();
		ASTNode copy = ASTNode.copySubtree(leftNode.getAST(), leftNode);
		minimizeNode(copy);
		//copy the ASTNodes from leftNode to copy so these still have a parent
		for (Iterator iterator = copy.structuralPropertiesForType().iterator(); iterator.hasNext();) {
			StructuralPropertyDescriptor cprop = (StructuralPropertyDescriptor) iterator.next();
			if(cprop.isChildListProperty()){
				List<ASTNode> values = (List<ASTNode>) leftNode.getStructuralProperty(cprop);
				List<ASTNode> target = (List<ASTNode>) copy.getStructuralProperty(cprop);
				while(!values.isEmpty()){
					ASTNode n = values.get(0);
					n.delete();
					target.add(n);
				}
			} else if (cprop.isChildProperty()){
				ASTNode value = (ASTNode) leftNode.getStructuralProperty(cprop);
				ASTNode vcopy = ASTNode.copySubtree(leftNode.getAST(), value);
				leftNode.setStructuralProperty(cprop, vcopy);
				copy.setStructuralProperty(cprop, value);
			} else {
				Object value = leftNode.getStructuralProperty(cprop);
				copy.setStructuralProperty(cprop, value);
			}
		}
		//install copy in the left ast
		if(prop != null){ //prop can be null in case one of the parents of the AST was modified (eg: Insert of a new Body)
			if(prop.isChildListProperty()){
				List<ASTNode> nodes = (List<ASTNode>) leftNode.getParent().getStructuralProperty(prop);
				int idx = 0;
				for(ASTNode n : nodes){
					if(n == leftNode){
						break;
					}
					idx++;
				}
				nodes.add(idx,copy);
			} else {
				leftNode.getParent().setStructuralProperty(prop, copy);
			}
		}
		//shouldnt do anything as node is unparented in step above
		leftNode.delete();
		
		//move left node
		if(property.isChildListProperty()){
			List<ASTNode> coll = (List<ASTNode>) newParent.getStructuralProperty(property);
			coll.add(index, leftNode);
		} else {
			newParent.setStructuralProperty(property, leftNode);
		}
		//clean up the node so that only mandatory properties remain
		
		minimizeNode(leftNode);
		addSubtreeMatching(leftMatching, rightMatching, leftNode, rightNode);
		return leftNode;
	}
	
	public String toString(){
		return "Move: " + original.toString();
	}
}
