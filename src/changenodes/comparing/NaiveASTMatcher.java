package changenodes.comparing;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.Statement;

public class NaiveASTMatcher extends ASTMatcher {

	public boolean match(MethodDeclaration decl, Object node){
		if (node instanceof MethodDeclaration) {
			MethodDeclaration otherDecl = (MethodDeclaration) node;
			StructuralPropertyDescriptor prop = MethodDeclaration.NAME_PROPERTY;
			SimpleName leftName = (SimpleName) decl.getStructuralProperty(prop);
			SimpleName rightName = (SimpleName) otherDecl.getStructuralProperty(prop);
			return this.match(leftName, rightName);
		}
		return false;
	}
	
	public boolean match(TypeDeclaration decl, Object node){
		if(node instanceof TypeDeclaration){
			TypeDeclaration otherDecl = (TypeDeclaration) node;
			StructuralPropertyDescriptor prop = TypeDeclaration.NAME_PROPERTY;
			SimpleName leftName = (SimpleName) decl.getStructuralProperty(prop);
			SimpleName rightName = (SimpleName) otherDecl.getStructuralProperty(prop);
			return this.match(leftName, rightName);
		}
		return false;
	}
	
	public boolean match(CompilationUnit unit, Object node){
		return node instanceof CompilationUnit;
	}
	
		
}
