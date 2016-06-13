package changenodes.operations;

import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;

public interface IOperation {

	public ASTNode apply(Map<ASTNode, ASTNode> leftMatching, Map<ASTNode, ASTNode> rightMatching);
	
	public IOperation setAffectedNode(ASTNode node);
	public ASTNode getAffectedNode();
	public ASTNode getOriginal();
	
	public boolean isDelete();
	public boolean isInsert();
	public boolean isMove();
	public boolean isUpdate();
}
