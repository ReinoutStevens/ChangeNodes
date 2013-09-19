package changenodes.comparing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;



/**
 * @author resteven
 */
public class PropertyDecider {
	public enum PropertyCategory {
	    IDENTIFIER, VALUE, CHILD
	}
	
	private static PropertyDecider instance;
	
	private Collection<StructuralPropertyDescriptor> identifierProperties = new HashSet<StructuralPropertyDescriptor>();
	private Collection<StructuralPropertyDescriptor> valueProperties = new HashSet<StructuralPropertyDescriptor>();
	
	
	public static PropertyDecider getInstance(){
		if(instance == null){
			instance = new PropertyDecider();
		}
		return instance;
	}
	
	
	public PropertyDecider(){
		initCollections();
	}
	
	public PropertyCategory decide(ASTNode node, StructuralPropertyDescriptor prop){
		return this.defaultBehaviour(node, prop);
	}
	
	
	public Collection<StructuralPropertyDescriptor> getPropertiesOfCategory(ASTNode node, PropertyCategory category){
		Collection<StructuralPropertyDescriptor> result = new ArrayList<StructuralPropertyDescriptor>();
		for (Iterator iterator = node.structuralPropertiesForType().iterator(); iterator.hasNext();) {
			StructuralPropertyDescriptor prop = (StructuralPropertyDescriptor) iterator.next();
			if(this.decide(node, prop) == category){
				result.add(prop);
			}
		}
		return result;
	}
	
	public Collection<StructuralPropertyDescriptor> getIdentifiers(ASTNode node){
		return this.getPropertiesOfCategory(node, PropertyCategory.IDENTIFIER);
	}
	
	public Collection<StructuralPropertyDescriptor> getValues(ASTNode node){
		return this.getPropertiesOfCategory(node, PropertyCategory.VALUE);
	}
	
	public Collection<StructuralPropertyDescriptor> getChildren(ASTNode node){
		return this.getPropertiesOfCategory(node, PropertyCategory.CHILD);
	}

	private PropertyCategory defaultBehaviour(ASTNode node, StructuralPropertyDescriptor prop){
		if(identifierProperties.contains(prop)){
			return PropertyCategory.IDENTIFIER;
		}
		if(valueProperties.contains(prop)){
			return PropertyCategory.VALUE;
		}
		if(prop.isSimpleProperty()){
			return PropertyCategory.VALUE;
		}
		
		return PropertyCategory.CHILD;
	}
	
	
	private void initCollections(){
		initMethodDeclaration();
		initTypeDeclaration();
		initFieldDeclaration();
		initPackageDeclaration();
	}
	
	private void initMethodDeclaration(){
		addIdentifierProperty(MethodDeclaration.NAME_PROPERTY);
		addValueProperty(MethodDeclaration.PARAMETERS_PROPERTY,
				MethodDeclaration.RETURN_TYPE_PROPERTY,
				MethodDeclaration.MODIFIERS2_PROPERTY);
	}
	
	private void initTypeDeclaration(){
		addIdentifierProperty(TypeDeclaration.NAME_PROPERTY);
		addValueProperty(TypeDeclaration.SUPERCLASS_PROPERTY,
				TypeDeclaration.SUPER_INTERFACES_PROPERTY,
				TypeDeclaration.MODIFIERS2_PROPERTY);
	}
	
	private void initFieldDeclaration(){
		addIdentifierProperty(FieldDeclaration.FRAGMENTS_PROPERTY); //this may not be the best key
		addValueProperty(FieldDeclaration.TYPE_PROPERTY,
				FieldDeclaration.MODIFIERS_PROPERTY,
				FieldDeclaration.MODIFIERS2_PROPERTY);
	}
	
	private void initPackageDeclaration(){
		addValueProperty(PackageDeclaration.NAME_PROPERTY);
	}
	
	
	private void addIdentifierProperty(StructuralPropertyDescriptor... props){
		for(StructuralPropertyDescriptor prop : props){
			identifierProperties.add(prop);
		}
	}
	
	private void addValueProperty(StructuralPropertyDescriptor... props){
		for(StructuralPropertyDescriptor prop : props){
			valueProperties.add(prop);
		}	
	}
}
