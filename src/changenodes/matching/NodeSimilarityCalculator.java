package changenodes.matching;

import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;

public interface NodeSimilarityCalculator {

	double calculateSimilarity(ASTNode left, ASTNode right);

	void setLeftMatching(Map<ASTNode, ASTNode> lMatching);
	void setRightMatching(Map<ASTNode, ASTNode> rMatching);

}
