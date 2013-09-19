package changenodes.matching;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.BodyDeclaration;

import changenodes.nodes.IChangeNode;


/**
 * @author resteven
 * Tries to match BodyDeclarations (thus: typedeclaration, methoddeclaration, etc.) from
 * one AST to another. At the moment we only match them when both are exactly the same.
 */
public class BodyDeclarationMatcher {
	
	public BodyDeclarationMatcher(){
		
	}
	
	
	public Map<ASTNode, ASTNode> match(ASTNode left, ASTNode right){
		Map<ASTNode, ASTNode> result = new HashMap<ASTNode, ASTNode>();
		BodyDeclarationVisitor leftVisitor = new BodyDeclarationVisitor();
		BodyDeclarationVisitor rightVisitor = new BodyDeclarationVisitor();
		ASTMatcher matcher = new ASTMatcher();
		
		left.accept(leftVisitor);
		right.accept(rightVisitor);
		
		Collection<BodyDeclaration> leftNodes = leftVisitor.getNodes();
		Collection<BodyDeclaration> rightNodes = rightVisitor.getNodes();
		
		for(BodyDeclaration leftBody : leftNodes){
			for(BodyDeclaration rightBody : rightNodes){
				if(leftBody.subtreeMatch(matcher, rightBody)){
					result.put(leftBody, rightBody);
				}
			}
		}
		
		return result;
	}
	
}
