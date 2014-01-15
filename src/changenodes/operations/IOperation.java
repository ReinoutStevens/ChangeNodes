package changenodes.operations;

import org.eclipse.jdt.core.dom.ASTNode;

public interface IOperation {

	public ASTNode apply();
	
	public IOperation setAffectedNode(ASTNode node);
	public ASTNode getAffectedNode();
	public ASTNode getOriginal();
}
