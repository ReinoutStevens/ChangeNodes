package changenodes.operations;

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

public class Update  extends Operation implements IOperation {

	private ASTNode original;
	private ASTNode rightParent, leftParent;
	private StructuralPropertyDescriptor property;
	
	/*
	 * Update the property of leftParent to the value of that property of the rightParent
	 */
	public Update(ASTNode original, ASTNode leftParent, ASTNode rightParent, StructuralPropertyDescriptor property){
		this.original = original;
		this.leftParent = leftParent;
		this.rightParent = rightParent;
		this.property = property;
	}

	public ASTNode getOriginal() {
		return original;
	}
	
	public ASTNode getLeftParent() {
		return leftParent;
	}

	public ASTNode getRightParent() {
		return rightParent;
	}

	public StructuralPropertyDescriptor getProperty() {
		return property;
	}
	
	public Object leftValue(){
		return leftParent.getStructuralProperty(property);
	}
	
	public Object rightValue(){
		return rightParent.getStructuralProperty(property);
	}
	
	public ASTNode getAffectedNode(){
		return leftParent;
	}
	
	public Update setAffectedNode(ASTNode node){
		return new Update(original, node, rightParent, property);
	}
	
	public ASTNode apply(Map<ASTNode, ASTNode> leftMatching, Map<ASTNode, ASTNode> rightMatching){
		if(property.isSimpleProperty()){
			Object value = rightParent.getStructuralProperty(property);
			leftParent.setStructuralProperty(property, value);
		} else {
			ASTNode node = (ASTNode) rightParent.getStructuralProperty(property);
			ASTNode copy = ASTNode.copySubtree(leftParent.getAST(), node);
			if(NodeClassifier.isLeafStatement(copy)){
				leftParent.setStructuralProperty(property, copy);
				addSubtreeMatching(leftMatching, rightMatching, copy, node);
			} else {
				for (Iterator iterator = copy.structuralPropertiesForType().iterator(); iterator.hasNext();) {
					StructuralPropertyDescriptor prop = (StructuralPropertyDescriptor) iterator.next();
					if(prop.isChildProperty()){
						ChildPropertyDescriptor cprop = (ChildPropertyDescriptor) prop;
						if(!cprop.isMandatory()){
							copy.setStructuralProperty(prop, null);
						}
					}
					else if(prop.isSimpleProperty()){
						SimplePropertyDescriptor cprop = (SimplePropertyDescriptor) prop;
						if(!cprop.isMandatory()){
							copy.setStructuralProperty(prop, null);
						}
					}
					else if(prop.isChildListProperty()){
						//shouldnt be possible
						assert(false);
					} 
				}
				if(property.isChildListProperty()){
					//should not happen
					assert(false);
				} else {
					leftParent.setStructuralProperty(property, copy);
				}
			}
			leftMatching.put(copy, node);
			rightMatching.put(node, copy);
		}
		
		return leftParent;
	}
	
	public String toString(){
		return "Update: " + original + " " + property.toString();
	}
}
