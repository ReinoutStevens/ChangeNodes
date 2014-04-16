package changenodes.operations;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ChildPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

public class Delete implements IOperation {

	ASTNode leftNode;
	ASTNode original;
	
	public Delete(ASTNode original, ASTNode leftNode){
		this.leftNode = leftNode;
		this.original = original;
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
	
	public ASTNode apply(){
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
	
	
	private boolean alreadyDeleted(){
		ASTNode current = leftNode;
		ASTNode parent = current.getParent();
		while(parent != null){
			current = parent;
			parent = current.getParent();
		}
		return !(current instanceof CompilationUnit);
	}
}
