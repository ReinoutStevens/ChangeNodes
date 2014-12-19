package changenodes.matching;

import org.eclipse.jdt.core.dom.ASTNode;

public class UnmatchingRootsException extends MatchingException {
	UnmatchingRootsException(ASTNode left, ASTNode right) {
		super(left, right);
	}
}
