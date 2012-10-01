package org.aspic.inference;



/**
 * The "/" binary built-in predicate.
 * @author mjs (matthew.south @ cancer.org.uk)
 *
 */
public class Division extends Binary {
	private static final String OPERATOR = "/";

	public Division(Element left, Element right) {
		super(OPERATOR, false, left, right);
	}
	
	//Henrik: Default constructor for Hibernate
	public Division(){
		super();
	}		
	
	public Constant apply(Substitution subs) {
		if (this.isGrounded()) { 
			if (getLeft() instanceof ConstantNumber && getRight() instanceof ConstantNumber) {
				return ((ConstantNumber) getLeft()).divide((ConstantNumber) getRight());
			} else {
				return this;
			}
		} else {
			Division newDivision = new Division(getLeft().apply(subs), getRight().apply(subs));
			newDivision.setKnowledgeBase(this.getKnowledgeBase());
			return newDivision;
		}
	}
}
