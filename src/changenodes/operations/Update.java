package changenodes.operations;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

public class Update extends Operation {

	private ASTNode left, right;
	private StructuralPropertyDescriptor property;
	
	
	public Update(ASTNode left, ASTNode right, StructuralPropertyDescriptor property){
		this.left = left;
		this.right = right;
		this.property = property;
	}

	public ASTNode getLeft() {
		return left;
	}

	public ASTNode getRight() {
		return right;
	}

	public StructuralPropertyDescriptor getProperty() {
		return property;
	}
	
	public Object leftValue(){
		return left.getStructuralProperty(property);
	}
	
	public Object rightValue(){
		return right.getStructuralProperty(property);
	}
	
	
	
	public ASTNode apply(){
		if(property.isSimpleProperty()){
			Object value = right.getStructuralProperty(property);
			left.setStructuralProperty(property, value);
		} else {
			ASTNode node = (ASTNode) right.getStructuralProperty(property);
			ASTNode value = ASTNode.copySubtree(AST.newAST(AST.JLS4), node);
			left.setStructuralProperty(property, value);
		}
		return left;
	}
}
