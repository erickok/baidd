package org.aspic.inference;



/**
 * The "+" binary built-in predicate.
 * 
 * @author mjs (matthew.south @ cancer.org.uk)
 *
 */
public class Addition extends Binary {
	private static final String OPERATOR = "+";

	/**
	 * Default constructor.
	 */
	public Addition(){
		super();
	}	
	
	/** 
	 * Typical constructor.
	 * @param left left operand.
	 * @param right right operand.
	 */
	public Addition(Element left, Element right) {
		super(OPERATOR, false, left, right);
	}
	
	public Constant apply(Substitution subs) {
		if (this.isGrounded()) { 
			if (getLeft() instanceof ConstantNumber && getRight() instanceof ConstantNumber) {
				return ((ConstantNumber) getLeft()).add((ConstantNumber) getRight());
			} else {
				return this;
			}
		} else {
			Addition newAddition = new Addition(getLeft().apply(subs), getRight().apply(subs));
			newAddition.setKnowledgeBase(this.getKnowledgeBase());
			return newAddition;
		}
	}
}
