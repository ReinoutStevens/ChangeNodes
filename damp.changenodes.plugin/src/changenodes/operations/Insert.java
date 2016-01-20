package changenodes.operations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ChildPropertyDescriptor;
import org.eclipse.jdt.core.dom.SimplePropertyDescriptor;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

import changenodes.matching.NodeClassifier;

public class Insert extends Operation implements IOperation {

	ASTNode original;
	ASTNode leftParent;
	ASTNode rightParent;
	ASTNode rightNode;
	ASTNode leftRemoved;
	ASTNode copy;
	int index;
	StructuralPropertyDescriptor property;
	
	public Insert(ASTNode original, ASTNode leftParent, ASTNode rightParent, ASTNode rightNode, StructuralPropertyDescriptor property, int index){
		this.original = original;
		this.leftParent = leftParent;
		this.rightParent = rightParent;
		this.rightNode = rightNode;
		this.property = property;
		this.index = index;
		this.leftRemoved = null;
		if(property.isChildProperty()){
			this.leftRemoved = (ASTNode) leftParent.getStructuralProperty(property);
		}
	}
	
	public ASTNode getAffectedNode(){
		return leftParent;
	}
	
	public Insert setAffectedNode(ASTNode node){
		return new Insert(original, node, rightParent, rightNode, property, index);
	}

	public ASTNode getOriginal() {
		return original;
	}
	
	public ASTNode getRightParent() {
		return rightParent;
	}

	public ASTNode getRightNode() {
		return rightNode;
	}
	
	public ASTNode getLeftRemoved() {
		return leftRemoved;
	}
	
	public ASTNode getCopy() {
		return copy;
	}

	public int getIndex() {
		if(!property.isChildListProperty())
			return -1;
		return index;
	}

	public StructuralPropertyDescriptor getProperty() {
		return property;
	}
	
	
	@Override
	public ASTNode apply(Map<ASTNode, ASTNode> leftMatching, Map<ASTNode, ASTNode> rightMatching) {
		copy = ASTNode.copySubtree(leftParent.getAST(), rightNode);
		/*if(NodeClassifier.isLeafStatement(copy)){
			if(property.isChildListProperty()){
				List<ASTNode> nodes = (List<ASTNode>) leftParent.getStructuralProperty(property);
				nodes.add(index, copy);
			} else {
				leftParent.setStructuralProperty(property, copy);
			}
			addSubtreeMatching(leftMatching, rightMatching, copy, rightNode);
		} else {*/
			minimizeNode(copy);
			if(property.isChildListProperty()){
				List<ASTNode> nodes = (List<ASTNode>) leftParent.getStructuralProperty(property);
				nodes.add(index, copy);
			} else {
				leftParent.setStructuralProperty(property, copy);
			}
			addSubtreeMatching(leftMatching, rightMatching, copy, rightNode);
			leftMatching.put(copy, rightNode);
			rightMatching.put(rightNode, copy);
		//}
		return copy;
	}
	
	public String toString(){
		return "Insert " + rightNode.toString();
	}
	
	public Collection<ASTNode> mandatoryNodes(){
		Collection<ASTNode> result = new ArrayList<ASTNode>();
		for (Iterator iterator = original.structuralPropertiesForType().iterator(); iterator.hasNext();) {
			StructuralPropertyDescriptor prop = (StructuralPropertyDescriptor) iterator.next();
			if(prop.isChildProperty()){
				ChildPropertyDescriptor cprop = (ChildPropertyDescriptor) prop;
				if(cprop.isMandatory()){
					ASTNode node = (ASTNode) original.getStructuralProperty(cprop);
					result.add(node);
				}
			}
		}
		return result;
	}
}
