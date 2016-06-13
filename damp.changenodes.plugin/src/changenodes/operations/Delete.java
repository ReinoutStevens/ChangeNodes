package changenodes.operations;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ChildPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

public class Delete extends Operation implements IOperation {

	ASTNode leftNode;
	ASTNode original;
	ASTNode leftParent = null;
	
	int index;
	
	public Delete(ASTNode original, ASTNode leftNode){
		this.leftNode = leftNode;
		if(leftNode != null){
			this.leftParent = leftNode.getParent();
		}
		this.original = original;
		this.index = calculateIndex(leftNode);
	}
	
	public boolean isDelete(){
		return true;
	}
	
	public ASTNode getOriginal() {
		return original;
	}
	
	public ASTNode getAffectedNode(){
		return leftNode;
	}
	
	public Delete setAffectedNode(ASTNode node){
		return new Delete(original, node);
	}
	
	public ASTNode getLeftParent() {
		return leftParent;
	}
	
	public ASTNode apply(Map<ASTNode, ASTNode> leftMatching, Map<ASTNode, ASTNode> rightMatching){
		/*StructuralPropertyDescriptor prop = leftNode.getLocationInParent();
		ASTNode parent = leftNode.getParent();
		if(prop.isChildListProperty()){
			List<ASTNode> nodes = (List<ASTNode>) parent.getStructuralProperty(prop);
			nodes.remove(leftNode);
		} else {
			parent.setStructuralProperty(prop, null);
		}*/
		if(!alreadyDeleted()){
			StructuralPropertyDescriptor prop = leftNode.getLocationInParent();
			if(prop.isChildProperty()){
				ChildPropertyDescriptor childProp = (ChildPropertyDescriptor) prop;
				if(childProp.isMandatory()){
					return leftNode;
				}
			}
			leftNode.delete();
		}
		return leftNode;
	}
	
	public String toString(){
		return "Delete: " + original.toString();
	}
	
	public int getIndex(){
		return index;
	}
	
	public int getOriginalIndex(){
		return calculateIndex(original);
	}
	
	private boolean alreadyDeleted(){
		ASTNode current = leftNode;
		ASTNode parent = current.getParent();
		while(parent != null){
			current = parent;
			parent = current.getParent();
		}
		return !(current instanceof CompilationUnit);
	}
	

	private int calculateIndex(ASTNode node){
		StructuralPropertyDescriptor prop = node.getLocationInParent();
		if(prop.isChildListProperty()){
			List<ASTNode> nodes = (List<ASTNode>) node.getParent().getStructuralProperty(prop);
			return nodes.indexOf(node);
		} else {
			return -1;
		}
	}
}
