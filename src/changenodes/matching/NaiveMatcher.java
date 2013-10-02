package changenodes.matching;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;

import changenodes.comparing.BreadthFirstNodeIterator;
import changenodes.comparing.INodeComparator;
import changenodes.comparing.NaiveComparator;
public class NaiveMatcher {

	INodeComparator comparator;
	Map<ASTNode, ASTNode> leftMatching;
		Map<ASTNode, ASTNode> rightMatching;
	
	
	
	public NaiveMatcher(){
		this(new NaiveComparator());
	}
	
	public NaiveMatcher(INodeComparator comparator){
		this.comparator = comparator;
		leftMatching = new HashMap<ASTNode, ASTNode>();
		rightMatching = new HashMap<ASTNode, ASTNode>();
	}
	
	public void match(ASTNode left, ASTNode right) throws MatchingException{
		for (Iterator<ASTNode> leftIterator = new BreadthFirstNodeIterator(left); leftIterator.hasNext();) {
			ASTNode leftNode = leftIterator.next();
			for (Iterator<ASTNode> rightIterator = new BreadthFirstNodeIterator(right); rightIterator.hasNext();) {
				ASTNode rightNode = rightIterator.next();
				if(comparator.compareKey(leftNode, rightNode)){
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
	
	public Map<ASTNode, ASTNode> getLeftMatching() {
		return leftMatching;
	}

	public Map<ASTNode, ASTNode> getRightMatching() {
		return rightMatching;
	}
	
}
