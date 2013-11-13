package changenodes.comparing;

import java.util.LinkedList;
import java.util.Queue;

import org.eclipse.jdt.core.dom.ASTNode;

public class BreadthFirstNodeIterator extends AbstractNodeIterator {

	
	private Queue<ASTNode> nodeQueue;

	public BreadthFirstNodeIterator(ASTNode start){
		this.nodeQueue = new LinkedList<ASTNode>();
		this.addNode(start);
	}
	@Override
	public boolean hasNext() {
		return !nodeQueue.isEmpty();
	}

	@Override
	protected void addNode(ASTNode node) {
		nodeQueue.add(node);
	}

	@Override
	protected ASTNode removeNode() {
		return nodeQueue.remove();
	}

	@Override
	protected ASTNode peek() {
		return nodeQueue.peek();
	}

}
