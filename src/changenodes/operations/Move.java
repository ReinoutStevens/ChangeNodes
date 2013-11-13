package changenodes.operations;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

public class Move implements IOperation {

	ASTNode newParent;
	ASTNode leftNode;
	StructuralPropertyDescriptor property;
	int index;
	
	public Move(ASTNode node, ASTNode newParent, StructuralPropertyDescriptor property, int index){
		this.newParent = newParent;
		this.leftNode = node;
		this.property = property;
		this.index = index;
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
	
	public Move setAffectedNode(ASTNode node){
		return new Move(node, newParent, property, index);
	}

	public StructuralPropertyDescriptor getProperty() {
		return property;
	}

	public int getIndex() {
		return index;
	}
	
	
	public ASTNode apply(){
		leftNode.delete();
		if(property.isChildListProperty()){
			List<ASTNode> coll = (List<ASTNode>) newParent.getStructuralProperty(property);
			coll.add(index, leftNode);
		} else {
			newParent.setStructuralProperty(property, leftNode);
		}
		return leftNode;
	}
	
	public String toString(){
		return "Move: " + leftNode.toString();
	}
}
