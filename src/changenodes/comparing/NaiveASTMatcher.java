package changenodes.comparing;

import java.util.Map;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.ASTNode;

public class NaiveASTMatcher extends ASTMatcher {

	
	private Map<ASTNode, ASTNode> leftMatching;
	private Map<ASTNode, ASTNode> rightMatching;

	public NaiveASTMatcher(Map<ASTNode,ASTNode> leftMatching, Map<ASTNode, ASTNode> rightMatching){
		this.leftMatching = leftMatching;
		this.rightMatching = rightMatching;
	}
	
	public boolean match(MethodDeclaration decl, Object node){
		if (node instanceof MethodDeclaration) {
			MethodDeclaration otherDecl = (MethodDeclaration) node;
			StructuralPropertyDescriptor prop = MethodDeclaration.NAME_PROPERTY;
			SimpleName leftName = (SimpleName) decl.getStructuralProperty(prop);
			SimpleName rightName = (SimpleName) otherDecl.getStructuralProperty(prop);
			return super.match(leftName, rightName);
		}
		return false;
	}
	
	public boolean match(TypeDeclaration decl, Object node){
		if(node instanceof TypeDeclaration){
			TypeDeclaration otherDecl = (TypeDeclaration) node;
			StructuralPropertyDescriptor prop = TypeDeclaration.NAME_PROPERTY;
			SimpleName leftName = (SimpleName) decl.getStructuralProperty(prop);
			SimpleName rightName = (SimpleName) otherDecl.getStructuralProperty(prop);
			return super.match(leftName, rightName);
		}
		return false;
	}
	
	public boolean match(CompilationUnit unit, Object node){
		return node instanceof CompilationUnit;
	}
	
	public boolean match(PackageDeclaration decl, Object node){
		return node instanceof PackageDeclaration;
	}
	
	//prevent names from randomly being matched
	public boolean match(SimpleName name, Object node){
		if(node instanceof SimpleName){
			SimpleName otherName = (SimpleName) node;
			ASTNode parent = name.getParent();
			ASTNode otherParent = otherName.getParent();
			if(leftMatching.containsKey(parent)){
				return leftMatching.get(parent).equals(otherParent);
			}
		}
		return false;
	}
	
	public boolean match(Modifier modifier, Object node){
		if(node instanceof Modifier){
			Modifier otherModifier = (Modifier) node;
			ASTNode parent = modifier.getParent();
			ASTNode otherParent = otherModifier.getParent();
			if(leftMatching.containsKey(parent)){
				return leftMatching.get(parent).equals(otherParent);
			}
		}
		return false;
	}
	
	public boolean match(PrimitiveType type, Object node){
		if(node instanceof PrimitiveType){
			PrimitiveType otherType = (PrimitiveType) node;
			ASTNode parent = type.getParent();
			ASTNode otherParent = otherType.getParent();
			if(leftMatching.containsKey(parent)){
				return leftMatching.get(parent).equals(otherParent);
			}
		}
		return false;
	}

		
}
