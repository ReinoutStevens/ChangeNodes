package changenodes.matching;

import org.eclipse.jdt.core.dom.ASTNode;

public abstract class MatchingException extends Exception {

	private ASTNode left;
	private ASTNode right;
	
	MatchingException(ASTNode left, ASTNode right){
		super();
		this.left = left;
		this.right = right;
	}

	public ASTNode getLeft() {
		return left;
	}

	public ASTNode getRight() {
		return right;
	}

}
