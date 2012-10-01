package org.aspic.inference;



/**
 * Abstract class for unary built-in predicates. Unary means "arity of 1". 
 * All built-in predicates that inherit from this class have a single argument,
 * <code>operand</code>.
 * @author mjs (matthew.south @ cancer.org.uk)
 *
 */
public abstract class Unary extends BuiltIn {
	
	/**
	 * Default constructor.
	 */
	public Unary(){
		super();
	}
	
	/**
	 * Typical constructor.
	 * @param operator unary built-in predicate's functor.
	 * @param operand single argument for this built in predicate
	 */
	public Unary(String operator, Element operand) {
		super(operator, new ElementList(operand));
	}
	/**
	 * Getter for operand.
	 * @return single argument for this built in predicate.
	 */
	public Element getOperand() {
		return super.getArg(0);
	}
	/**
	 * Setter for operand.
	 * @param operand single argument for this built in predicate.
	 */
	public void setOperand(Element operand) {
		operand.setKnowledgeBase(this.getKnowledgeBase());
		super.setArg(0, operand);
	}
}
