package changenodes.matching;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;

import changenodes.comparing.BreadthFirstNodeIterator;
import changenodes.comparing.NaiveASTMatcher;

public class SubtreeMatcher implements IMatcher{
	ASTMatcher matcher = new NaiveASTMatcher();
	private HashMap<ASTNode, ASTNode> leftMatching;
	private HashMap<ASTNode, ASTNode> rightMatching;
	
	
	public SubtreeMatcher(){
		leftMatching = new HashMap<ASTNode, ASTNode>();
		rightMatching = new HashMap<ASTNode, ASTNode>();
	}
	
	@Override
	public void match(ASTNode left, ASTNode right) throws MatchingException {
		for (Iterator<ASTNode> leftIterator = new BreadthFirstNodeIterator(left); leftIterator.hasNext();) {
			ASTNode leftNode = leftIterator.next();
			for (Iterator<ASTNode> rightIterator = new BreadthFirstNodeIterator(right); rightIterator.hasNext();) {
				ASTNode rightNode = rightIterator.next();
				if(leftNode.subtreeMatch(matcher, rightNode)){
					if(leftMatching.containsKey(leftNode)){
						throw new AlreadyMatchedException(leftNode, rightNode);
					}
					if(rightMatching.containsKey(rightNode)){
						throw new AlreadyMatchedException(rightNode, leftNode);
					}
					leftMatching.put(leftNode, rightNode);
					rightMatching.put(rightNode, leftNode);
				}
			}
		}
	}

	@Override
	public Map<ASTNode, ASTNode> getLeftMatching() {
		return leftMatching;
	}

	@Override
	public Map<ASTNode, ASTNode> getRightMatching() {
		return rightMatching;
	}

}
