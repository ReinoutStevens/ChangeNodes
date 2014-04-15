package changenodes.operations;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ChildPropertyDescriptor;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

public class Move implements IOperation {

	ASTNode original;
	ASTNode newParent;
	ASTNode leftNode;
	StructuralPropertyDescriptor property;
	int index;
	
	public Move(ASTNode original, ASTNode node, ASTNode newParent, StructuralPropertyDescriptor property, int index){
		this.original = original;
		this.newParent = newParent;
		this.leftNode = node;
		this.property = property;
		this.index = index;
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
	
	public Move setAffectedNode(ASTNode node){
		return new Move(original, node, newParent, property, index);
	}

	public StructuralPropertyDescriptor getProperty() {
		return property;
	}

	public int getIndex() {
		return index;
	}
	
	public ASTNode apply(){
		boolean unparent = true;
		StructuralPropertyDescriptor prop = leftNode.getLocationInParent();
		if(prop != null){
			if(prop.isChildProperty()){
			ChildPropertyDescriptor childprop = (ChildPropertyDescriptor) prop;
			unparent = !childprop.isMandatory();
			}
		}
		if(!unparent){
			//node that is mandatory in its parent but that will be moved
			//hopefully either the parent is deleted in a later stage, or another node is moved to this location
			//may not be correct though...
			ASTNode copy = ASTNode.copySubtree(leftNode.getAST(), leftNode);
			leftNode.getParent().setStructuralProperty(prop, copy);
		}
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
		return "Move: " + original.toString();
	}
}
