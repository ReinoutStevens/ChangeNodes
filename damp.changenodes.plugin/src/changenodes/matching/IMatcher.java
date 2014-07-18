package changenodes.matching;

import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;

public interface IMatcher {
	public void match(ASTNode left, ASTNode right) throws MatchingException;
	public Map<ASTNode,ASTNode> getLeftMatching();
	public Map<ASTNode,ASTNode> getRightMatching();
	
}
