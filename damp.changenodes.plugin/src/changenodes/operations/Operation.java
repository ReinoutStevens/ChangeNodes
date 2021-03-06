package changenodes.operations;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ChildPropertyDescriptor;
import org.eclipse.jdt.core.dom.SimplePropertyDescriptor;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

public abstract class Operation implements IOperation {

	protected void addSubtreeMatching(Map<ASTNode, ASTNode> leftMatching, Map<ASTNode, ASTNode> rightMatching, ASTNode left, ASTNode right){
		if(left == null){
			return;
		}
		if(left.getNodeType() != right.getNodeType()){
			return;
		}
		if(rightMatching.containsKey(right)){
			//we are re-inserting a node that already had a match
			ASTNode r = rightMatching.get(right);
			leftMatching.remove(r);
		}
		leftMatching.put(left, right);
		rightMatching.put(right, left);
		for (Iterator iterator = left.structuralPropertiesForType().iterator(); iterator.hasNext();) {
			StructuralPropertyDescriptor prop = (StructuralPropertyDescriptor) iterator.next();
			if(prop.isChildProperty()){
				//if(((ChildPropertyDescriptor) prop).isMandatory()){
					ASTNode leftNode = (ASTNode) left.getStructuralProperty(prop);
					ASTNode rightNode = (ASTNode) right.getStructuralProperty(prop);
					addSubtreeMatching(leftMatching, rightMatching, leftNode, rightNode);
				//}
				
			} else if(prop.isChildListProperty()){
				List<ASTNode> leftNodes = (List<ASTNode>) left.getStructuralProperty(prop);
				List<ASTNode> rightNodes = (List<ASTNode>) right.getStructuralProperty(prop);
				for(int i = 0; i < leftNodes.size(); ++i){
					addSubtreeMatching(leftMatching, rightMatching, leftNodes.get(i), rightNodes.get(i));
				}
			}
		}
	}
	
	protected void addMinimalSubtreeMatching(Map<ASTNode, ASTNode> leftMatching, Map<ASTNode, ASTNode> rightMatching, ASTNode left, ASTNode right){
		if(left == null){
			return;
		}
		if(left.getNodeType() != right.getNodeType()){
			return;
		}
		if(rightMatching.containsKey(right)){
			//we are re-inserting a node that already had a match
			ASTNode r = rightMatching.get(right);
			leftMatching.remove(r);
		}
		leftMatching.put(left, right);
		rightMatching.put(right, left);
		for (Iterator iterator = left.structuralPropertiesForType().iterator(); iterator.hasNext();) {
			StructuralPropertyDescriptor prop = (StructuralPropertyDescriptor) iterator.next();
			if(prop.isChildProperty()){
				if(((ChildPropertyDescriptor) prop).isMandatory()){
					ASTNode leftNode = (ASTNode) left.getStructuralProperty(prop);
					ASTNode rightNode = (ASTNode) right.getStructuralProperty(prop);
					addMinimalSubtreeMatching(leftMatching, rightMatching, leftNode, rightNode);
				}
			}
		}
	}
	
	
	protected void minimizeNode(ASTNode node){
		for (Iterator iterator = node.structuralPropertiesForType().iterator(); iterator.hasNext();) {
			StructuralPropertyDescriptor prop = (StructuralPropertyDescriptor) iterator.next();
			if(prop.isChildProperty()){
				ChildPropertyDescriptor cprop = (ChildPropertyDescriptor) prop;
				if(!cprop.isMandatory()){
					node.setStructuralProperty(prop, null);
				} else {
					ASTNode propNode = (ASTNode) node.getStructuralProperty(prop);
					minimizeNode(propNode);
				}
			}
			else if(prop.isSimpleProperty()){
				SimplePropertyDescriptor cprop = (SimplePropertyDescriptor) prop;
				if(!cprop.isMandatory()){
					node.setStructuralProperty(prop, null);
				}
			}
			else if(prop.isChildListProperty()){
				Collection<ASTNode> nodes = (Collection<ASTNode>) node.getStructuralProperty(prop);
				nodes.clear();
			} 
		}
	}
	
	public boolean isMove(){
		return false;
	}

	public boolean isDelete(){
		return false;
	}
	
	public boolean isInsert(){
		return false;
	}
	
	public boolean isUpdate(){
		return false;
	}
}
