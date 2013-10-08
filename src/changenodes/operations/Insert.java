package changenodes.operations;

import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

public class Insert extends Operation {

	ASTNode leftParent;
	ASTNode rightParent;
	ASTNode rightNode;
	int index;
	StructuralPropertyDescriptor property;
	
	public Insert(ASTNode leftParent, ASTNode rightParent, ASTNode rightNode, StructuralPropertyDescriptor property, int index){
		this.leftParent = leftParent;
		this.rightParent = rightParent;
		this.rightNode = rightNode;
		this.property = property;
		this.index = index;
	}

	public ASTNode getRightParent() {
		return rightParent;
	}

	public ASTNode getRightNode() {
		return rightNode;
	}

	public int getIndex() {
		if(!property.isChildListProperty())
			return -1;
		return index;
	}

	public StructuralPropertyDescriptor getProperty() {
		return property;
	}
	
	
	public ASTNode apply(){
		ASTNode copy = ASTNode.copySubtree(leftParent.getAST(), rightNode);
		if(property.isChildListProperty()){
			List<ASTNode> nodes = (List<ASTNode>) leftParent.getStructuralProperty(property);
			nodes.add(index, copy);
		} else {
			leftParent.setStructuralProperty(property, copy);
		}
		return copy;
	}
	
}
