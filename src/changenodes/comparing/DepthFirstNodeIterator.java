package changenodes.comparing;

import java.util.Collection;
import java.util.Iterator;
import java.util.Stack;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.ChildPropertyDescriptor;
import org.eclipse.jdt.core.dom.SimplePropertyDescriptor;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

public class DepthFirstNodeIterator extends AbstractNodeIterator {

	private Stack<ASTNode> nodeStack;
	
	public DepthFirstNodeIterator(ASTNode start){
		this(start, new PropertyDecider());
	}
	
	public DepthFirstNodeIterator(ASTNode start, PropertyDecider decider){
		this.nodeStack = new Stack<ASTNode>();
		this.decider = decider;
		this.addNode(start);
	}

	@Override
	public boolean hasNext() {
		return !nodeStack.isEmpty();
	}

	
	@Override
	protected void addNode(ASTNode node) {
		nodeStack.push(node);		
	}

	@Override
	protected ASTNode removeNode() {
		return nodeStack.pop();
	}

	@Override
	protected ASTNode peek() {
		return nodeStack.peek();
	}
	
}
