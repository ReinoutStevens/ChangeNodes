package changenodes.comparing;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

public class NaiveComparator implements INodeComparator {
	PropertyDecider decider;
	
	public NaiveComparator(){
		this.decider = PropertyDecider.getInstance();
	}
	
	@SuppressWarnings("rawtypes")
	public boolean compareKey(ASTNode left, ASTNode right){
		if(left.getNodeType() != right.getNodeType()){
			return false;
		}
		StructuralPropertyDescriptor leftProp, rightProp;
		leftProp = findIdentifierProperty(left);
		rightProp = findIdentifierProperty(right);
		if(leftProp == null && rightProp == null){
			return true;
		}
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
			if(!this.compareProperty(left, right, prop)){
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
			if(!this.compareProperty(left, right, prop)){
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

	@Override
	public boolean compareProperty(ASTNode left, ASTNode right,
			StructuralPropertyDescriptor property) {
		Object leftObject = left.getStructuralProperty(property);
		Object rightObject = right.getStructuralProperty(property);
		if(property.isSimpleProperty()){
			return leftObject.equals(rightObject);
		} else if(property.isChildProperty()){
			ASTNode leftNode = (ASTNode) leftObject;
			ASTNode rightNode = (ASTNode) rightObject;
			return this.compare(leftNode, rightNode);
		} else if(property.isChildListProperty()){
			Collection<ASTNode> leftNodes = (Collection<ASTNode>) leftObject;
			Collection<ASTNode> rightNodes = (Collection<ASTNode>) rightObject;
			return this.compare(leftNodes, rightNodes);
		}
		return false;
	}
	
	
}
