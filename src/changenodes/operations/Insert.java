package changenodes.operations;

import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

public class Insert implements IOperation {

	ASTNode original;
	ASTNode leftParent;
	ASTNode rightParent;
	ASTNode rightNode;
	int index;
	StructuralPropertyDescriptor property;
	
	public Insert(ASTNode original, ASTNode leftParent, ASTNode rightParent, ASTNode rightNode, StructuralPropertyDescriptor property, int index){
		this.original = original;
		this.leftParent = leftParent;
		this.rightParent = rightParent;
		this.rightNode = rightNode;
		this.property = property;
		this.index = index;
	}
	
	public ASTNode getAffectedNode(){
		return leftParent;
	}
	
	public Insert setAffectedNode(ASTNode node){
		return new Insert(original, node, rightParent, rightNode, property, index);
	}

	public ASTNode getOriginal() {
		return original;
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
	
	public String toString(){
		return "Insert " + rightNode.toString();
	}
	
}
