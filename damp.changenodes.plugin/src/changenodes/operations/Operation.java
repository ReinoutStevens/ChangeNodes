package changenodes.operations;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

public abstract class Operation implements IOperation {

	protected void addSubtreeMatching(Map<ASTNode, ASTNode> leftMatching, Map<ASTNode, ASTNode> rightMatching, ASTNode left, ASTNode right){
		if(left == null && right == null){
			return;
		}
		leftMatching.put(left, right);
		rightMatching.put(right, left);
		for (Iterator iterator = left.structuralPropertiesForType().iterator(); iterator.hasNext();) {
			StructuralPropertyDescriptor prop = (StructuralPropertyDescriptor) iterator.next();
			if(prop.isChildProperty()){
				ASTNode leftNode = (ASTNode) left.getStructuralProperty(prop);
				ASTNode rightNode = (ASTNode) right.getStructuralProperty(prop);
				addSubtreeMatching(leftMatching, rightMatching, leftNode, rightNode);
			} else if(prop.isChildListProperty()){
				List<ASTNode> leftNodes = (List<ASTNode>) left.getStructuralProperty(prop);
				List<ASTNode> rightNodes = (List<ASTNode>) right.getStructuralProperty(prop);
				assert(leftNodes.size() == rightNodes.size());
				for(int i = 0; i < leftNodes.size(); ++i){
					addSubtreeMatching(leftMatching, rightMatching, leftNodes.get(i), rightNodes.get(i));
				}
			}
		}
	}

}
