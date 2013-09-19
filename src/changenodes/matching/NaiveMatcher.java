package changenodes.matching;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;

import changenodes.comparing.BreadthFirstNodeIterator;
import changenodes.comparing.NaiveComparator;
public class NaiveMatcher {

	NaiveComparator comparator = new NaiveComparator();
	
	public Map<ASTNode, ASTNode> match(ASTNode left, ASTNode right){
		Map<ASTNode, ASTNode> result = new HashMap<ASTNode, ASTNode>();
		for (Iterator<ASTNode> leftIterator = new BreadthFirstNodeIterator(left); leftIterator.hasNext();) {
			ASTNode leftNode = leftIterator.next();
			for (Iterator<ASTNode> rightIterator = new BreadthFirstNodeIterator(right); rightIterator.hasNext();) {
				ASTNode rightNode = rightIterator.next();
				if(comparator.compareKey(leftNode, rightNode)){
					result.put(leftNode, rightNode);
				}
			}
		}
		return result;
	}
}
