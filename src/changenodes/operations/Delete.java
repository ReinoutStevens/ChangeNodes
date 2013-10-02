package changenodes.operations;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

public class Delete extends Operation {

	ASTNode leftNode;
	
	public Delete(ASTNode leftNode){
		this.leftNode = leftNode;
	}
	
	public ASTNode getLeftNode(){
		return leftNode;
	}
	
	public ASTNode apply(){
		StructuralPropertyDescriptor prop = leftNode.getLocationInParent();
		ASTNode parent = leftNode.getParent();
		if(prop.isChildListProperty()){
			List<ASTNode> nodes = (List<ASTNode>) parent.getStructuralProperty(prop);
			nodes.remove(leftNode);
		} else {
			parent.setStructuralProperty(prop, null);
		}
		return leftNode;
	}
}
