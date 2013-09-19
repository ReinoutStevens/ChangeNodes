package changenodes.nodes;

import java.util.Collection;

public interface IChangeNode {

	public Collection<IChangeNode> getChildren();
	
	public Integer getNodeType();
}
