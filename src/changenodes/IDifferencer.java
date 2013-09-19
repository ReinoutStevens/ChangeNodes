package changenodes;

import java.util.Collection;

import changenodes.operations.IOperation;



public interface IDifferencer {

	public void difference();
	public Collection<IOperation> getOperations();
}
