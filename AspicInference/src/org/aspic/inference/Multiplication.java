package org.aspic.inference;



/**
 * The "*" binary built-in predicate.
 * @author mjs (matthew.south @ cancer.org.uk)
 *
 */
public class Multiplication extends Binary {
	private static final String OPERATOR = "*";

	/**
	 * Typical constructor.
	 * @param left left operand.
	 * @param right right operand.
	 */
	public Multiplication(Element left, Element right) {
		super(OPERATOR, false, left, right);
	}
	
	/**
	 * Default constructor.
	 */
	public Multiplication(){
		super();
	}		
		
	public Constant apply(Substitution subs) {
		if (this.isGrounded()) { 
			if (getLeft() instanceof ConstantNumber && getRight() instanceof ConstantNumber) {
				return ((ConstantNumber) getLeft()).multiply((ConstantNumber) getRight());
			} else {
				return this;
			}
		} else {
			Multiplication newMultiplication = new Multiplication(getLeft().apply(subs), getRight().apply(subs));
			newMultiplication.setKnowledgeBase(this.getKnowledgeBase());
			return newMultiplication;
		}
	}
}
