package changenodes.matching;

import org.eclipse.jdt.core.dom.ASTNode;

public class NodeClassifier {

	   public static boolean isLeafStatement(ASTNode node){
	    	//dragons

	    	int type = node.getNodeType();
	    	switch(type){
	    	case ASTNode.ARRAY_ACCESS:
	    	case ASTNode.ARRAY_CREATION:
	    	case ASTNode.ARRAY_INITIALIZER:
	    	case ASTNode.ARRAY_TYPE:
	    	case ASTNode.BOOLEAN_LITERAL:
	    	case ASTNode.BREAK_STATEMENT:
	    	case ASTNode.BLOCK_COMMENT:
	    	case ASTNode.CHARACTER_LITERAL:
	    	case ASTNode.CLASS_INSTANCE_CREATION:
	    	case ASTNode.EMPTY_STATEMENT:
	    	case ASTNode.EXPRESSION_STATEMENT:
	    	case ASTNode.ENUM_CONSTANT_DECLARATION:
	    	case ASTNode.FIELD_ACCESS:
	    	case ASTNode.FIELD_DECLARATION:
	    	case ASTNode.IMPORT_DECLARATION:
	    	case ASTNode.JAVADOC:
	    	case ASTNode.LINE_COMMENT:
	    	case ASTNode.METHOD_INVOCATION:
	    	case ASTNode.MARKER_ANNOTATION:
	    	//case ASTNode.MODIFIER: //is too specific, gives very poor matches
	    	case ASTNode.NULL_LITERAL:
	    	case ASTNode.NUMBER_LITERAL:
	    	case ASTNode.PACKAGE_DECLARATION:
	    	case ASTNode.PARAMETERIZED_TYPE:
	    	//case ASTNode.PRIMITIVE_TYPE:
	    	case ASTNode.QUALIFIED_NAME:
	    	case ASTNode.QUALIFIED_TYPE:
	    	case ASTNode.SIMPLE_NAME:
	    	case ASTNode.SIMPLE_TYPE:
	    	case ASTNode.SINGLE_VARIABLE_DECLARATION:
	    	case ASTNode.STRING_LITERAL:
	    	case ASTNode.SUPER_FIELD_ACCESS:
	    	case ASTNode.SUPER_METHOD_INVOCATION:
	    	case ASTNode.SUPER_CONSTRUCTOR_INVOCATION:
	    	case ASTNode.TYPE_LITERAL:
	    	case ASTNode.TYPE_PARAMETER:
	    	case ASTNode.VARIABLE_DECLARATION_FRAGMENT:
	    	case ASTNode.VARIABLE_DECLARATION_STATEMENT:
	    		return true;
	    	default:
	    		return false;
	    	}		
	    }	
	   
	   public static boolean isComment(ASTNode node){
		   int type = node.getNodeType();
		   switch(type){
		   case ASTNode.BLOCK_COMMENT:
		   case ASTNode.JAVADOC:
		   case ASTNode.LINE_COMMENT:
			   return true;
		   default:	
			   return false;
		   }
	   }
	   
	   public static boolean isRoot(ASTNode node){
		   return node.getParent() == null;
	   }
}
