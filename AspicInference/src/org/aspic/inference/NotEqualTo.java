package org.aspic.inference;



/** 
 * The "\==" binary evaluator built-in predicate. 
 * 
 * @author mjs (matthew.south @ cancer.org.uk).
 */
public class NotEqualTo extends BinaryEvaluator {
	private static final String OPERATOR = "\\==";

	/**
	 * Typical constructor.
	 * @param left left operand.
	 * @param right right operand.
	 */
	public NotEqualTo(Element left, Element right) {
		super(OPERATOR, false, left, right);
	}
	
	/**
	 * Default constructor.
	 */
	public NotEqualTo(){
		super();
	}		
			
	public Constant apply(Substitution subs) {
		if (this.isGrounded()) { 
			return this; 
		} else {
			NotEqualTo newIn = new NotEqualTo(getLeft().apply(subs), getRight().apply(subs));
			newIn.setKnowledgeBase(this.getKnowledgeBase());
			return newIn;
		}
	}
	
	protected boolean evaluate(Element left, Element right) {
		return !(left.isEqualModuloVariables(right));
	}
}
