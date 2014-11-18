package changenodes.comparing;

import java.util.Collection;
import java.util.Iterator;


import org.eclipse.jdt.core.dom.ASTNode;

import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

public abstract class AbstractNodeIterator implements Iterator<ASTNode>, Iterable<ASTNode>{


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
		for (Iterator iterator = top.structuralPropertiesForType().iterator(); iterator.hasNext();) {
			StructuralPropertyDescriptor prop = (StructuralPropertyDescriptor) iterator.next();
			Object value = top.getStructuralProperty(prop);
			processProperty(prop, value);
		}
		return top;
	}
	
	
	public Iterator<ASTNode> iterator(){
		return this;
	}
	
	private void processProperty(StructuralPropertyDescriptor prop, Object value){
		if(prop.isSimpleProperty()){
			processSimpleProperty(prop, value);
		}
		if(prop.isChildProperty()){
			processChildProperty(prop, value);
		}
		if(prop.isChildListProperty()){
			processChildListProperty(prop, value);
		}
	}
	
		
	private void processChildProperty(StructuralPropertyDescriptor prop, Object object){
		assert(prop.isChildProperty());
		ASTNode value = (ASTNode) object;
		if(value != null){
			this.addNode(value);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void processChildListProperty(StructuralPropertyDescriptor prop, Object object){
		assert(prop.isChildListProperty());
		Collection<ASTNode> values = (Collection<ASTNode>) object;
		for(ASTNode value : values){
			this.addNode(value);
		}
	}
	
	private void processSimpleProperty(StructuralPropertyDescriptor prop, Object object){
		assert(prop.isSimpleProperty());
		//nothing
	}
}