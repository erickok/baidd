package org.aspic.inference;



/** 
 * The ">=" binary evaluator built-in predicate.
 * 
 * @author mjs (matthew.south @ cancer.org.uk).
 */
public class GreaterThanOrEqualTo extends ArithmeticBinaryEvaluator {
	private static final String OPERATOR = ">=";

	/**
	 * Typical constructor.
	 * @param left left operand.
	 * @param right right operand.
	 */
	public GreaterThanOrEqualTo(Element left, Element right) {
		super(OPERATOR, false, left, right);
	}
	
	/**
	 * Default constructor.
	 */
	public GreaterThanOrEqualTo(){
		super();
	}			
	
	public Constant apply(Substitution subs) {
		if (this.isGrounded()) { 
			return this; 
		} else {
			GreaterThanOrEqualTo newIn = new GreaterThanOrEqualTo(getLeft().apply(subs), getRight().apply(subs));
			newIn.setKnowledgeBase(this.getKnowledgeBase());
			return newIn;
		}
	}
	
	protected boolean evaluate(ConstantNumber left, ConstantNumber right) {
		return (left.getNumber().floatValue() >= right.getNumber().floatValue());
	}
}
