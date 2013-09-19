package changenodes.matching;

import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;



public class BodyDeclarationVisitor extends ASTVisitor {

	Collection<BodyDeclaration> nodes;
	
	
	public Collection<BodyDeclaration> getNodes(){
		return nodes;
	}
	
	public BodyDeclarationVisitor(){
		super();
		this.nodes = new LinkedList<BodyDeclaration>();
	}
	
	
	public boolean visit(MethodDeclaration node){
		this.visitBodyDeclaration(node);
		return super.visit(node);
	}
	

	public boolean visit(TypeDeclaration node){
		this.visitBodyDeclaration(node);
		return super.visit(node);
	}
	

	public boolean visit(EnumDeclaration node){
		this.visitBodyDeclaration(node);
		return super.visit(node);
	}
	
	public boolean visit(AnnotationTypeDeclaration node){
		this.visitBodyDeclaration(node);
		return super.visit(node);
	}
	
	public boolean visit(AnnotationTypeMemberDeclaration node){
		this.visitBodyDeclaration(node);
		return super.visit(node);
	}
	
	public boolean visit(EnumConstantDeclaration node){
		this.visitBodyDeclaration(node);
		return super.visit(node);
	}
	
	public boolean visit(FieldDeclaration node){
		this.visitBodyDeclaration(node);
		return super.visit(node);
	}
	
	
	public boolean visit(Initializer node){
		this.visitBodyDeclaration(node);
		return super.visit(node);
	}
	
	
	private void visitBodyDeclaration(BodyDeclaration node){
		nodes.add(node);
	}
}
