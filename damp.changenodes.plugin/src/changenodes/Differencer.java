package changenodes;

import java.beans.PropertyDescriptor;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

import changenodes.comparing.BreadthFirstNodeIterator;
import changenodes.comparing.DepthFirstNodeIterator;
import changenodes.matching.BestLeafTreeMatcher;
import changenodes.matching.IMatcher;
import changenodes.matching.MatchingException;
import changenodes.matching.NodeClassifier;
import changenodes.operations.*;

public class Differencer implements IDifferencer {
    private static final int UP = 1;
    private static final int LEFT = 2;
    private static final int DIAG = 3;
	
	private ASTNode left;
	private ASTNode right;
	
	private ASTNode leftOriginal;
	private ASTNode rightOriginal;
	private Map<ASTNode, ASTNode> mapCopyToOriginal;
	
	private Collection<IOperation> operations;
	private Map<ASTNode,ASTNode> leftMatching;
	private Map<ASTNode,ASTNode> rightMatching;
	private Map<ASTNode,ASTNode> leftMatchingPrime;
	private Map<ASTNode,ASTNode> rightMatchingPrime;
	private List<ASTNode> outOfOrder;
	
	private IMatcher matcher;
	
	public Differencer(ASTNode left, ASTNode right){
		//copy left tree as we will be modifying it
		AST ast = AST.newAST(AST.JLS8);
		this.leftOriginal = left;
		this.left = ASTNode.copySubtree(ast, left);
		this.rightOriginal = right;
		this.right = right; //right tree should not be modified, so we can keep it
		this.matcher = new BestLeafTreeMatcher();
		this.outOfOrder = new LinkedList<ASTNode>();
	}
	
	/* left is modified during execution, use it for debugging */
	public ASTNode getLeft(){
		return left;
	}
	
	public ASTNode getRight(){
		return right;
	}
	
	@Override
	public void difference() throws MatchingException {
		mapCopyToOriginal = matchOriginalAndCopy();
		
		//E is an empty list of operations
		operations = new LinkedList<IOperation>();
		outOfOrder = new LinkedList<ASTNode>();
		//initialize M
		leftMatchingPrime = new HashMap<ASTNode, ASTNode>();
		rightMatchingPrime = new HashMap<ASTNode, ASTNode>();
		matcher = new BestLeafTreeMatcher();
		partialMatching();
		//M' <- M
		//For some reason ChangeDistiller does a regular = here
		//afaik this should be wrong
		leftMatchingPrime.putAll(leftMatching);
		rightMatchingPrime.putAll(rightMatching);
		
		for (Iterator<ASTNode> rightBFT = new BreadthFirstNodeIterator(right); rightBFT.hasNext();) {
			ASTNode current = rightBFT.next();
			ASTNode parent = current.getParent();
			if(NodeClassifier.isComment(current)){
				continue;
			}
			if(parent != right.getParent()){ //we are not working on the root
				ASTNode currentPartner = rightMatchingPrime.get(current);
				ASTNode parentPartner = rightMatchingPrime.get(parent); 
				if(currentPartner == null){ //if x has no partner in M'
					StructuralPropertyDescriptor prop = current.getLocationInParent();
					int index = -1;
					IOperation operation;
					if(prop.isChildListProperty()){
						index = findPosition(current);
						operation = insert(parentPartner, parent, current, prop, index);
					} else {
						//We are inserting a 'property' that has a unique value in the ast, meaning we delete the original value
						//Instead of outputting a delete+insert we output an update
						Object parentValue = parent.getStructuralProperty(prop);
						Object parentPartnerValue = parentPartner.getStructuralProperty(prop);
						boolean shouldUpdate = false;
						//one of the 2 can be null, for example a method that returns void and another that returns int
						//the void one will have a propertyvalue of null
						if(parentValue == null || parentPartnerValue == null){ 
							shouldUpdate = (parentValue != parentPartnerValue);
						} else {
							shouldUpdate = !parentValue.toString().equals(parentPartnerValue.toString());
						}
						if(shouldUpdate){
							Update update = new Update(getOriginal(parentPartner), parentPartner, parent, prop);
							operation = update;
							update.apply();
							//both nodes should be equal now
							addSubtreeMatching((ASTNode) update.leftValue(), current);
							addOperation(operation);
						} else {
							if(prop.isChildProperty()){ //these 2 are equal but dont match, lets match them
								leftMatchingPrime.put((ASTNode)parentPartner.getStructuralProperty(prop), current);
								rightMatchingPrime.put(current, (ASTNode)parentPartner.getStructuralProperty(prop));
							}
						}
					}
				} else { //x has a partner
					ASTNode  partnerParent = currentPartner.getParent();
					//check whether there is a value in current and partner that differs
					update(currentPartner, current);
					//node are miss aligned
					if(!rightMatchingPrime.get(parent).equals(partnerParent)){
						assert(!leftMatchingPrime.get(partnerParent).equals(parent));
						ASTNode newParent = rightMatchingPrime.get(parent);
						move(currentPartner, partnerParent, newParent, current, parent);
						
					}
				}
			}
			alignChildren(rightMatchingPrime.get(current), current);
		}
		delete();
	}

	@Override
	public Collection<IOperation> getOperations() {
		return operations;
	}
	
	
	private void partialMatching() throws MatchingException{
		matcher.match(left, right);
		leftMatching = matcher.getLeftMatching();
		rightMatching = matcher.getRightMatching();
	}
	
	
	/* Updates the simple properties of leftNode to rightNode.
	 * Other properties are updated at a later point in time as they are ASTNodes
	 */
	private void update(ASTNode left, ASTNode right){
		List<StructuralPropertyDescriptor> properties = (List<StructuralPropertyDescriptor>) right.structuralPropertiesForType();
		for(StructuralPropertyDescriptor prop : properties){
			if(prop.isSimpleProperty()){
				Object leftObj = left.getStructuralProperty(prop);
				Object rightObj = right.getStructuralProperty(prop);
				if(leftObj == null && rightObj == null){
					return;
				}
				if((leftObj == null && rightObj != null) || 
						(leftObj != null && rightObj == null) || 
						!left.getStructuralProperty(prop).equals(right.getStructuralProperty(prop))){
					Update update = new Update(getOriginal(left), left, right, prop);
					addOperation(update);
					update.apply();
				}
			}
		}
	}
	
	private Insert insert(ASTNode parentPartner, ASTNode parent,ASTNode current,StructuralPropertyDescriptor prop,int index){
		Insert insert = new Insert(getOriginal(parentPartner), parentPartner, parent, current, prop, index);
		ASTNode newNode = insert.apply();
		leftMatchingPrime.put(newNode, current);
		rightMatchingPrime.put(current, newNode);
		insertChildren(newNode, current);
		addOperation(insert);
		return insert;
	}
	
	/*
	 * recursively outputs Insert operations for a newly inserted node
	 * dont know whether this is actually useful or we should just output the parent node
	 * note that we will not find corresponding nodes in the left tree
	 * as we are inserting a bunch of new nodes that also have a newly introduced parent
	 * 
	 * Removed calls to this as output was incorrect in case parts of a subtree matched
	 * but the parent node did not. In this case the parent node would add the children,
	 * and the matched children would not get moved
	 */
	private void insertChildren(ASTNode newNode, ASTNode otherNode){
		for (Iterator iterator = newNode.structuralPropertiesForType().iterator(); iterator.hasNext();) {
			StructuralPropertyDescriptor prop = (StructuralPropertyDescriptor) iterator.next();
			Object lValue, rValue;
			lValue = newNode.getStructuralProperty(prop);
			rValue = otherNode.getStructuralProperty(prop);
			if(lValue != null && rValue != null){
				if(prop.isChildProperty()){
					ASTNode lNode = (ASTNode) lValue;
					ASTNode rNode = (ASTNode) rValue;
					Insert insert = new Insert(getOriginal(newNode), newNode,otherNode,rNode, prop, -1);
					leftMatchingPrime.put(lNode, rNode);
					rightMatchingPrime.put(rNode,lNode);
					addOperation(insert);
					insertChildren(lNode, rNode);
				} else if(prop.isChildListProperty()){
					List<ASTNode> lChildren = (List<ASTNode>) lValue;
					List<ASTNode> rChildren = (List<ASTNode>) rValue;
					for(int i = 0; i < lChildren.size(); ++i){
						ASTNode lNode = lChildren.get(i);
						ASTNode rNode = rChildren.get(i);
						//Insert insert = new Insert(getOriginal(newNode), newNode,otherNode,rNode, prop, i);
						leftMatchingPrime.put(lNode, rNode);
						rightMatchingPrime.put(rNode,lNode);
						//addOperation(insert);
						insertChildren(lNode, rNode);
					}
				} else {
					//Objects
					//Update update = new Update(getOriginal(newNode), newNode, otherNode, prop);
					//addOperation(update);
				}
			}
		}
	}
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void alignChildren(ASTNode left, ASTNode right){
		List leftProps = left.structuralPropertiesForType();
		
		for (Iterator iterator = leftProps.iterator(); iterator.hasNext();) {
			StructuralPropertyDescriptor prop = (StructuralPropertyDescriptor) iterator.next();
			Object leftObj = left.getStructuralProperty(prop);
			Object rightObj = right.getStructuralProperty(prop);
			if(prop.isChildListProperty()){ 
				List<ASTNode> leftNodes = (List<ASTNode>) leftObj;
				List<ASTNode> rightNodes = (List<ASTNode>) rightObj;
				alignCollections(left, right, leftNodes, rightNodes);
			}
		}
	}
	
	private void alignCollections(ASTNode left, ASTNode right, List<ASTNode> lefts, List<ASTNode> rights){
		//let S1 be the sequence of children of left whose partners are children of right
		//let S2 be the sequence of children of right whose partners are children of left
		List<ASTNode> leftPartners = new LinkedList<ASTNode>();
		List<ASTNode> rightPartners = new LinkedList<ASTNode>();
		//mark all children of left and right as out of order
		outOfOrder.addAll(lefts);
		outOfOrder.addAll(rights);
		//Assume we only have matching in the same propertydescriptor in left/right
		for(ASTNode leftNode : lefts){
			ASTNode match = leftMatching.get(leftNode);
			if(match != null){
				leftPartners.add(leftNode);
				rightPartners.add(match);
			}
		}
		
		Map<ASTNode, ASTNode> longestSequence = longestCommonSubsequence(leftPartners, rightPartners);
		//for each pair in sequence mark nodes as in order
		for(ASTNode key : longestSequence.keySet()){
			outOfOrder.remove(key);
			outOfOrder.remove(longestSequence.get(key));
		}
		//
		for(ASTNode leftNode : leftPartners){
			if(!longestSequence.containsKey(leftNode) && leftMatching.containsKey(leftNode)){
				ASTNode partner = leftMatching.get(leftNode);
				move(leftNode, left, left, partner, right);
				outOfOrder.remove(leftNode);
				outOfOrder.remove(partner);
			}
		}
		
	}

	public void move(ASTNode node, ASTNode parent, ASTNode newParent, ASTNode rightNode, ASTNode rightParent){
		int position = -1;
		StructuralPropertyDescriptor prop = rightNode.getLocationInParent();
		Move move;
		if(prop.isChildListProperty()){
			position = findPosition(rightNode);
		} 
		move = new Move(getOriginal(node), node, newParent, rightNode, prop, position);
		addOperation(move);
		move.apply();
	}
	
	
	private void delete(){
		//loop over left AST and see whether there are nodes that are not matched
		List<Delete> deletes = new LinkedList<Delete>();
		for (Iterator<ASTNode> iterator = new DepthFirstNodeIterator(left); iterator.hasNext();) {
			ASTNode node = iterator.next();
			if(NodeClassifier.isComment(node)){
				continue;
			}
			if(node != null && !leftMatchingPrime.containsKey(node)){
				boolean parentAlreadyDeleted = false;
				found:
				for(Delete delete : deletes){
					ASTNode potentialParent = delete.getAffectedNode();
					ASTNode parent = node;
					while(parent != null){
						if(parent.equals(potentialParent)){
							parentAlreadyDeleted = true;
							break found;
						}
						parent = parent.getParent();
					}
				}
				if(!parentAlreadyDeleted){
					Delete delete = new Delete(getOriginal(node), node);
					deletes.add(delete);
				}
			}
		}
		//apply deletes (so not to mess up our iterator)
		//deletes can probably be cleaner by deleting just the parent node and not the parent node + all children
		for(Delete delete : deletes){
			delete.apply();
		}
		operations.addAll(deletes);
	}
	
	
	//taken from changedistiller
	private Map<ASTNode, ASTNode> longestCommonSubsequence(List<ASTNode> lefts, List<ASTNode> rights) {
		int m = lefts.size();
		int n = rights.size();

		int[][] c = new int[m + 1][n + 1];
		int[][] b = new int[m + 1][n + 1];

		for (int i = 0; i <= m; i++) {
			c[i][0] = 0;
			b[i][0] = 0;
		}
		for (int i = 0; i <= n; i++) {
			c[0][i] = 0;
			b[0][i] = 0;
		}

		for (int i = 1; i <= m; i++) {
			for (int j = 1; j <= n; j++) {
				ASTNode left = lefts.get(i - 1);
				ASTNode right = rights.get(j - 1);
				ASTNode matched = leftMatching.get(left);
				if (matched != null && matched.equals(right)) {
					c[i][j] = c[i - 1][j - 1] + 1;
					b[i][j] = DIAG;
				} else if (c[i - 1][j] >= c[i][j - 1]) {
					c[i][j] = c[i - 1][j];
					b[i][j] = UP;
				} else {
					c[i][j] = c[i][j - 1];
					b[i][j] = LEFT;
				}
			}
		}
		Map<ASTNode, ASTNode> result = new HashMap<ASTNode, ASTNode>();
		extractLCS(b, lefts, rights, m, n, result);
		return result;
	}

	private void extractLCS(int[][] b, List<ASTNode> l, List<ASTNode> r, int i, int j, Map<ASTNode, ASTNode> lcs) {
		if ((i != 0) && (j != 0)) {
			if (b[i][j] == DIAG) {
				lcs.put(l.get(i-1), r.get(j-1));
				extractLCS(b, l, r, i - 1, j - 1, lcs);
			} else if (b[i][j] == UP) {
				extractLCS(b, l, r, i - 1, j, lcs);
			} else {
				extractLCS(b, l, r, i, j - 1, lcs);
			}
		}
	}
	
	//BUG: probably a bug here as we do not follow the paper 100%...
	private int findPosition(ASTNode node){
		StructuralPropertyDescriptor property = node.getLocationInParent();
		ASTNode parent = node.getParent();
		assert(property.isChildListProperty());
		List<ASTNode> children = (List<ASTNode>) parent.getStructuralProperty(property);
		
		ASTNode previousSibling = getPreviousSibling(node, children);
		//while(previousSibling != null && outOfOrder.contains(previousSibling)){
		//	previousSibling = getPreviousSibling(previousSibling, children);
		//}
        // x is the leftmost child of y that is marked "in order"
		if(previousSibling == null){
			return 0;
		}
		ASTNode partner = rightMatchingPrime.get(previousSibling);
		assert(partner != null);
		// 5. Suppose u is the ith child of its parent
        // (counting from left to right) that is marked "in order"
        // return i+1
        int count = 0;
        ASTNode partnerParent = partner.getParent();
        List<ASTNode> partnerChildren = (List<ASTNode>) partnerParent.getStructuralProperty(property);
        for(ASTNode current : partnerChildren){
        	if(current.equals(partner)){
        		break;
        	}
        	if(!outOfOrder.contains(current)){
        			count++;
        	}	
        }	
        return count + 1;
	}
	
	
	private ASTNode getPreviousSibling(ASTNode node, List<ASTNode> children){
		int i = getChildIndex(node, children);
		if(i == 0){
			return null;
		}
		return children.get(i - 1);
	}
	
	

	private int getChildIndex(ASTNode child, List<ASTNode> children){
		int i =  children.indexOf(child);
		assert(i >= 0);
		return i;
	}
	
	
	private void addOperation(IOperation operation){
		operations.add(operation);
	}
	
	private void addSubtreeMatching(ASTNode left, ASTNode right){
		if(left == null && right == null){
			return;
		}
		leftMatchingPrime.put(left, right);
		rightMatchingPrime.put(right, left);
		for (Iterator iterator = left.structuralPropertiesForType().iterator(); iterator.hasNext();) {
			StructuralPropertyDescriptor prop = (StructuralPropertyDescriptor) iterator.next();
			if(prop.isChildProperty()){
				ASTNode leftNode = (ASTNode) left.getStructuralProperty(prop);
				ASTNode rightNode = (ASTNode) right.getStructuralProperty(prop);
				addSubtreeMatching(leftNode, rightNode);
			} else if(prop.isChildListProperty()){
				List<ASTNode> leftNodes = (List<ASTNode>) left.getStructuralProperty(prop);
				List<ASTNode> rightNodes = (List<ASTNode>) right.getStructuralProperty(prop);
				assert(leftNodes.size() == rightNodes.size());
				for(int i = 0; i < leftNodes.size(); ++i){
					addSubtreeMatching(leftNodes.get(i), rightNodes.get(i));
				}
			}
		}
	}
	

	private Map<ASTNode, ASTNode> matchOriginalAndCopy(){
		Map<ASTNode, ASTNode> result = new HashMap<ASTNode, ASTNode>();
		
		DepthFirstNodeIterator origIt = new DepthFirstNodeIterator(leftOriginal);
		for (DepthFirstNodeIterator copyIt = new DepthFirstNodeIterator(left); copyIt.hasNext();) {
			ASTNode copy = copyIt.next();
			ASTNode orig = origIt.next();
			if(copy != null){
				assert(orig != null);
				result.put(copy, orig);
			}
		}
		return result;
	}
	
	private ASTNode getOriginal(ASTNode copy){
		assert(mapCopyToOriginal != null);
		ASTNode result = mapCopyToOriginal.get(copy);
		if(!mapCopyToOriginal.containsKey(copy)){
			return null;
		}
		return result;
	}
}
