package changenodes.operations;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

public class Update implements IOperation {

	private ASTNode rightParent, leftParent;
	private StructuralPropertyDescriptor property;
	
	/*
	 * Update the property of leftParent to the value of that property of the rightParent
	 */
	public Update(ASTNode leftParent, ASTNode rightParent, StructuralPropertyDescriptor property){
		this.leftParent = leftParent;
		this.rightParent = rightParent;
		this.property = property;
	}

	public ASTNode getLeftParent() {
		return leftParent;
	}

	public ASTNode getRightParent() {
		return rightParent;
	}

	public StructuralPropertyDescriptor getProperty() {
		return property;
	}
	
	public Object leftValue(){
		return leftParent.getStructuralProperty(property);
	}
	
	public Object rightValue(){
		return rightParent.getStructuralProperty(property);
	}
	
	public ASTNode getAffectedNode(){
		return leftParent;
	}
	
	public Update setAffectedNode(ASTNode node){
		return new Update(node, rightParent, property);
	}
	
	public ASTNode apply(){
		if(property.isSimpleProperty()){
			Object value = rightParent.getStructuralProperty(property);
			leftParent.setStructuralProperty(property, value);
		} else {
			ASTNode node = (ASTNode) rightParent.getStructuralProperty(property);
			ASTNode value = ASTNode.copySubtree(leftParent.getAST(), node);
			leftParent.setStructuralProperty(property, value);
		}
		return leftParent;
	}
	
	public String toString(){
		return "Update: " + leftParent.toString() + " " + property.toString();
	}

}
