package changenodes.comparing;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

public class NaiveComparator {
	PropertyDecider decider = new PropertyDecider();
	
	@SuppressWarnings("rawtypes")
	public boolean compareKey(ASTNode left, ASTNode right){
		if(left.getNodeType() != right.getNodeType()){
			return false;
		}
		StructuralPropertyDescriptor leftProp, rightProp;
		leftProp = findIdentifierProperty(left);
		rightProp = findIdentifierProperty(right);
		if(leftProp == null || rightProp == null){
			return false;
		}
		Object leftObject = left.getStructuralProperty(leftProp);
		Object rightObject = right.getStructuralProperty(rightProp);
		return this.compare(leftObject, rightObject);
	}
	
	
	public boolean compareValues(ASTNode left, ASTNode right){
		if(left == null || right == null){
			return left == right;
		}
		if(left.getNodeType() != right.getNodeType()){
			return false;
		}
		for(StructuralPropertyDescriptor prop : decider.getValues(left)){
			if(prop.isChildProperty()) {
				ASTNode leftProp = (ASTNode) left.getStructuralProperty(prop);
				ASTNode rightProp = (ASTNode) right.getStructuralProperty(prop);
				if(!this.compare(leftProp, rightProp))
					return false;
			}
			else if(prop.isChildListProperty()) {
				Collection<ASTNode> leftProps = (Collection<ASTNode>) left.getStructuralProperty(prop);
				Collection<ASTNode> rightProps = (Collection<ASTNode>) right.getStructuralProperty(prop);
				if(!this.compare(leftProps, rightProps))
					return false;
			} else if(!this.compare(left.getStructuralProperty(prop), right.getStructuralProperty(prop))){
				return false;
			}
		}
		return true;
	}
	
	
	private StructuralPropertyDescriptor findIdentifierProperty(ASTNode node){
		for (Iterator iterator = node.structuralPropertiesForType().iterator(); iterator.hasNext();) {
			StructuralPropertyDescriptor type = (StructuralPropertyDescriptor) iterator.next();
			PropertyDecider.PropertyCategory category = decider.decide(node, type);
			if(category == PropertyDecider.PropertyCategory.IDENTIFIER){
				return type;
			}
		}
		return null;
	}
	
	
	public boolean compare(Object left, Object right){
		if(left == null || right == null){
			return left == right;
		}
		if (right instanceof ASTNode) {
			ASTNode rightNode = (ASTNode) right;
			if (left instanceof ASTNode) {
				ASTNode leftNode = (ASTNode) left;
				return this.compare(leftNode, rightNode);
			}
		}
		return left.equals(right);
	}
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public boolean compare(ASTNode left, ASTNode right){
		if(left == null || right == null){
			return left == right;
		}
		if(left.getNodeType() != right.getNodeType()){
			return false;
		}
		for (Iterator iterator = left.structuralPropertiesForType().iterator(); iterator.hasNext();) {
			StructuralPropertyDescriptor prop = (StructuralPropertyDescriptor) iterator.next();
			if(prop.isChildProperty()) {
				ASTNode leftProp = (ASTNode) left.getStructuralProperty(prop);
				ASTNode rightProp = (ASTNode) right.getStructuralProperty(prop);
				if(!this.compare(leftProp, rightProp))
					return false;
			}
			else if(prop.isChildListProperty()) {
				Collection<ASTNode> leftProps = (Collection<ASTNode>) left.getStructuralProperty(prop);
				Collection<ASTNode> rightProps = (Collection<ASTNode>) right.getStructuralProperty(prop);
				if(!this.compare(leftProps, rightProps))
					return false;
			} else if(!this.compare(left.getStructuralProperty(prop), right.getStructuralProperty(prop))){
				return false;
			}
		}
		return true;
	}
	
	public boolean compare(Collection<ASTNode> lefts, Collection<ASTNode> rights){
		for(ASTNode left : lefts){
			boolean rightHasSame = false;
			for(ASTNode right: rights){
				if(this.compare(left, right))
					rightHasSame = true;
			}
			if(!rightHasSame){
				return false;
			}
		}
		return true;
	}
}
