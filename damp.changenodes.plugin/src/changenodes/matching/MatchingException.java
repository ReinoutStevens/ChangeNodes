package changenodes.matching;

import org.eclipse.jdt.core.dom.ASTNode;

public abstract class MatchingException extends Exception {

	private static final long serialVersionUID = -2030971010525400680L;
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
