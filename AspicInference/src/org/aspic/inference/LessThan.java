package org.aspic.inference;



/** 
 * The "<" binary evaluator built-in predicate. 
 * 
 * @author mjs (matthew.south @ cancer.org.uk).
 */
public class LessThan extends ArithmeticBinaryEvaluator {
	private static final String OPERATOR = "<";

	/**
	 * Typical constructor.
	 * @param left left operand.
	 * @param right right operand.
	 */
	public LessThan(Element left, Element right) {
		super(OPERATOR, false, left, right);
	}
	
	/**
	 * Default constructor.
	 */
	public LessThan(){
		super();
	}		
	
	public Constant apply(Substitution subs) {
		if (this.isGrounded()) { 
			return this; 
		} else {
			LessThan newIn = new LessThan(getLeft().apply(subs), getRight().apply(subs));
			newIn.setKnowledgeBase(this.getKnowledgeBase());
			return newIn;
		}
	}
	
	protected boolean evaluate(ConstantNumber left, ConstantNumber right) {
		return (left.getNumber().floatValue() < right.getNumber().floatValue());
	}
}
