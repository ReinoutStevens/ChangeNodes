package changenodes.comparing;

import java.util.Collection;
import java.util.Iterator;
import java.util.Stack;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.ChildPropertyDescriptor;
import org.eclipse.jdt.core.dom.SimplePropertyDescriptor;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

public abstract class AbstractNodeIterator implements Iterator<ASTNode>{

	protected PropertyDecider decider;

	@Override
	public abstract boolean hasNext();

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Cannot remove nodes");
	}
	
	protected abstract void addNode(ASTNode node);
	protected abstract ASTNode removeNode();
	protected abstract ASTNode peek();
	
	
	@Override
	public ASTNode next() {
		ASTNode top = this.removeNode();
		Collection<StructuralPropertyDescriptor> childProperties = decider.getChildren(top);
		if(childProperties.isEmpty()){
			return top;
		}
		for(StructuralPropertyDescriptor prop : childProperties){
			Object propValue = top.getStructuralProperty(prop);
			if(prop.isSimpleProperty()){
				processProperty((SimplePropertyDescriptor) prop, propValue);
			}
			if(prop.isChildProperty()){
				processProperty((ChildPropertyDescriptor) prop, propValue);
			}
			if(prop.isChildListProperty()){
				processProperty((ChildListPropertyDescriptor) prop, propValue);
			}
		}
		return top;
	}
	
		
	private void processProperty(ChildPropertyDescriptor prop, Object object){
		ASTNode value = (ASTNode) object;
		if(value != null){
			this.addNode(value);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void processProperty(ChildListPropertyDescriptor prop, Object object){
		Collection<ASTNode> values = (Collection<ASTNode>) object;
		for(ASTNode value : values){
			this.addNode(value);
		}
	}
	
	private void processProperty(SimplePropertyDescriptor prop, Object object){
		//nothing
	}
	

}