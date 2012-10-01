package org.aspic.inference;



/**
 * <p>Abstract class for binary built-in predicates.  A binary predicate
 * has two named arguments, <code>left</code> and <code>right</code>.
 * All binary built in predicates should inherit from this class.</p>
 * <p>Standard Terms used prefix notation.  A binary Term, e.g. <code>+(id, 2)</code>  might also be expressed 
 * as <code>id+2</code> using infix notation.  Binary Terms have an
 * infix field that affects the way they are shown in <code>inspect</code>.</p>
 * 
 * @author mjs (matthew.south @ cancer.org.uk)
 */
public abstract class Binary extends BuiltIn {
	private boolean infix = false; // used for inspect

	/**
	 * Typical constructor.
	 * @param operator binary built-in predicate's functor.
	 * @param infix flag for "inspect using infix notation".
	 * @param left left operand
	 * @param right right operand
	 */
	public Binary(String operator, boolean infix, Element left, Element right) {
		super(operator, new ElementList(left, right));
		this.infix = infix;
	}
	
	/**
	 * Default constructor.
	 */
	public Binary(){
		super();
	}
		
	/**
	 * Getter for boolean infix flag.
	 * @return infix flag
	 */
	public boolean isInfix() {
		return infix;
	}
	
	/**
	 * Setter for boolean infix field.
	 * @param infix flag for "inspect using infix notation".
	 */
	public void setInfix(boolean infix) {
		this.infix = infix;
	}
	
	/**
	 * Getter for left operand.
	 * @return left operand.
	 */
	public Element getLeft() {
		return super.getArg(0);
	}
	
	/** 
	 * Setter for left operand.
	 * @param left left operand.
	 */
	public void setLeft(Element left) {
		left.setKnowledgeBase(this.getKnowledgeBase());
		super.setArg(0, left);
	}
	
	/**
	 * Getter for right operand.
	 * @return right operand.
	 */
	public Element getRight() {
		return super.getArg(1);
	}
	
	/** 
	 * Setter for right operand.
	 * @param right right operand.
	 */
	public void setRight(Element right) {
		right.setKnowledgeBase(this.getKnowledgeBase());
		super.setArg(1, right);
	}

	@Override
	public String inspect() {
		if (infix==true) {
			return getLeft().inspect() + " " + this.getFunctor() + " " + getRight().inspect();
		} else {
			return this.getFunctor() + "(" + getLeft().inspect() + ", " + getRight().inspect() + ")";			
		}
	}
}
