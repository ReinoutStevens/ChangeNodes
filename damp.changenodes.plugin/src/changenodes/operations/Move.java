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
	StructuralPropertyDescriptor property;
	int index;
	
	public Move(ASTNode original, ASTNode node, ASTNode newParent, ASTNode rightNode, StructuralPropertyDescriptor property, int index){
		this.original = original;
		this.newParent = newParent;
		this.leftNode = node;
		this.property = property;
		this.index = index;
		this.rightNode = rightNode;
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
		//clean up the node so that only mandatory properties remain
		ASTNode newNode;
		if(property.isChildListProperty()){
			List<ASTNode> nodes = (List<ASTNode>) newParent.getStructuralProperty(property);
			newNode = nodes.get(index);
		} else {
			newNode = (ASTNode) newParent.getStructuralProperty(property);
		}
		for (Iterator iterator = newNode.structuralPropertiesForType().iterator(); iterator.hasNext();) {
			prop = (StructuralPropertyDescriptor) iterator.next();
			if(prop.isChildProperty()){
				ChildPropertyDescriptor cprop = (ChildPropertyDescriptor) prop;
				if(!cprop.isMandatory()){
					newNode.setStructuralProperty(prop, null);
				}
			}
			else if(prop.isSimpleProperty()){
				SimplePropertyDescriptor cprop = (SimplePropertyDescriptor) prop;
				if(!cprop.isMandatory()){
					newNode.setStructuralProperty(prop, null);
				}
			}
			else if(prop.isChildListProperty()){
				Collection<ASTNode> nodes = (Collection<ASTNode>) newNode.getStructuralProperty(prop);
				nodes.clear();
			} 
		}
		return leftNode;
	}
	
	public String toString(){
		return "Move: " + original.toString();
	}
}
