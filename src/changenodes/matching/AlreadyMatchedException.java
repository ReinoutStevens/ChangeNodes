package changenodes.matching;

import org.eclipse.jdt.core.dom.ASTNode;

public class AlreadyMatchedException extends MatchingException {

	
	AlreadyMatchedException(ASTNode left, ASTNode right) {
		super(left, right);
	}

	private static final long serialVersionUID = 1467905473556633727L;

}
