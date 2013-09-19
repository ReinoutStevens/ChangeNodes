package changenodes.comparing;

import changenodes.nodes.IChangeNode;

public interface INodeComparator {

	boolean compare(IChangeNode left, IChangeNode right);
}
