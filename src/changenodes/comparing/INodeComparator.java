package changenodes.comparing;

import java.util.Collection;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

public interface INodeComparator {

	boolean compare(ASTNode left, ASTNode right);
	boolean compare(Object left, Object right);
	boolean compare(Collection<ASTNode> left, Collection<ASTNode> right);
	boolean compareKey(ASTNode left, ASTNode right);
	boolean compareValues(ASTNode left, ASTNode right);
	
	boolean compareProperty(ASTNode left, ASTNode right, StructuralPropertyDescriptor property);
}
