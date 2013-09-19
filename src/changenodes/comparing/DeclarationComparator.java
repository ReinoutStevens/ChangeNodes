package changenodes.comparing;

import changenodes.nodes.IChangeNode;

public class DeclarationComparator implements INodeComparator {

	@Override
	public boolean compare(IChangeNode left, IChangeNode right) {
		return left.getNodeType().equals(right.getNodeType());
	}

	
}
