package changenodes.operations;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

public class Move implements IOperation {

	ASTNode parent, newParent;
	ASTNode leftNode;
	StructuralPropertyDescriptor property;
	int index;
	
	public Move(ASTNode parent, ASTNode newParent, ASTNode node, StructuralPropertyDescriptor property, int index){
		this.parent = parent;
		this.newParent = newParent;
		this.leftNode = node;
		this.property = property;
		this.index = index;
	}

	public ASTNode getParent() {
		return parent;
	}

	public ASTNode getNewParent() {
		return newParent;
	}

	public ASTNode getLeftNode() {
		return leftNode;
	}

	public StructuralPropertyDescriptor getProperty() {
		return property;
	}

	public int getIndex() {
		return index;
	}
	
	
	public ASTNode apply(){
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
