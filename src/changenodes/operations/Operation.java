package changenodes.operations;

import org.eclipse.jdt.core.dom.ASTNode;

public abstract class Operation implements IOperation {

	abstract public ASTNode apply();
}
