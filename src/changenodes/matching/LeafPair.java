package changenodes.matching;

import org.eclipse.jdt.core.dom.ASTNode;

public class LeafPair implements Comparable<LeafPair>{

	ASTNode left;
	ASTNode right;
	double similarity;
	
	public LeafPair(ASTNode left, ASTNode right, double similarity){
		this.left = left;
		this.right = right;
		this.similarity = similarity;
	}
	
	public ASTNode getLeft() {
		return left;
	}

	public ASTNode getRight() {
		return right;
	}

	public double getSimilarity() {
		return similarity;
	}

	@Override
	public int compareTo(LeafPair o) {
        return -Double.compare(similarity, o.getSimilarity());
	}
}
