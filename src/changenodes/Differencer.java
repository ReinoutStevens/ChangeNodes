package changenodes;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jdt.core.dom.ASTNode;

import changenodes.matching.BodyDeclarationMatcher;
import changenodes.operations.*;

public class Differencer implements IDifferencer {
	private ASTNode left;
	private ASTNode right;
	
	private Collection<IOperation> operations;
	private Map<ASTNode,ASTNode> matching;
	private BodyDeclarationMatcher matcher;
	
	public Differencer(ASTNode left, ASTNode right){
		this.left = left;
		this.right = right;
		this.matcher = new BodyDeclarationMatcher();
	}
	
	
	@Override
	public void difference() {
		operations = new LinkedList<IOperation>();
		partialMatching();
		update();
	}

	@Override
	public Collection<IOperation> getOperations() {
		return operations;
	}
	
	
	private void partialMatching(){
		this.matching = matcher.match(left, right);
	}
	
	
	private void update(){
		
	}


	
}
