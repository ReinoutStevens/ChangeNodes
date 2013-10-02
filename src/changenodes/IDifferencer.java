package changenodes;

import java.util.Collection;

import changenodes.matching.MatchingException;
import changenodes.operations.IOperation;



public interface IDifferencer {

	public void difference() throws MatchingException;
	public Collection<IOperation> getOperations();
}
