package org.aspic.inference;



/**
 * The "-" binary evaluator built-in predicate.
 * A built in binary evaluator class for substraction with predicate "-".
 * @author mjs (matthew.south @ cancer.org.uk)
 *
 */
public class Subtraction extends Binary {
	private static final String OPERATOR = "-";

	/**
	 * Typical constructor.
	 * @param left left operand.
	 * @param right right operand.
	 */
	public Subtraction(Element left, Element right) {
		super(OPERATOR, false, left, right);
	}
	
	/**
	 * Default constructor.
	 */
	public Subtraction(){
		super();
	}		

	public Constant apply(Substitution subs) {
		if (this.isGrounded()) { 
			if (getLeft() instanceof ConstantNumber && getRight() instanceof ConstantNumber) {
				return ((ConstantNumber) getLeft()).subtract((ConstantNumber) getRight());
			} else {
				return this;
			}
		} else {
			Subtraction newSubtraction = new Subtraction(getLeft().apply(subs), getRight().apply(subs));
			newSubtraction.setKnowledgeBase(this.getKnowledgeBase());
			return newSubtraction;
		}
	}
}
