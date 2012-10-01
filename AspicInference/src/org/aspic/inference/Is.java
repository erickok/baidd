package org.aspic.inference;

import java.util.Iterator;
import java.util.logging.Logger;


/** 
 * The "is" unary built-in predicate.  This predicate allows you to 
 * compare two numbers or unify a Variable with a number.
 * The Variable must the left-hand operand, not the right.
 * 
 * @author mjs (matthew.south @ cancer.org.uk).
 */
public class Is extends Binary {
	private static Logger logger = Logger.getLogger(Is.class.getName());		
	private static final String OPERATOR = "is";
	
	/**
	 * Default constructor.
	 */
	public Is(){
		super();
	}
	
	
	/**
	 * Typical constructor.
	 * @param left left operand.
	 * @param right right operand.
	 */
	public Is(Element left, Element right) {
		super(OPERATOR, false, left, right);
	}
	
	public Constant apply(Substitution subs) {
		if (this.isGrounded()) { 
			return this; 
		} else {
			Is newIn = new Is(getLeft().apply(subs), getRight().apply(subs));
			newIn.setKnowledgeBase(this.getKnowledgeBase());
			return newIn;
		}
	}

	Iterator<RuleArgument> argumentIterator(Double needed, Party party, int level, int d_top, RuleArgumentValuator valuator, boolean restrictedRebutting) {
		Substitution subs = new Substitution();
//		 this apply is used to evaluate and numerical arithmetic, the subs is discarded.
		Element candidate = this.getRight().apply(subs); 
		// first compare two numbers
		if ((this.getLeft() instanceof ConstantNumber) && (candidate instanceof ConstantNumber)) {
			// check that argument is valid.  NB float values used in a weak attempt to surpress rounding errors in division.
			if (((ConstantNumber) getLeft()).getNumber().floatValue() == ((ConstantNumber) candidate).getNumber().floatValue()) {
				Rule topRule = new Rule(Is.this);
				topRule.setKnowledgeBase(this.getKnowledgeBase());
				RuleArgument argument = new RuleArgument(topRule, new Double(1.0), subs, new RuleArgumentList(), party, level, d_top, valuator, restrictedRebutting);
				logger.fine(party + ": found " + argument.getName() + " : " + argument.inspect());
				return new SingleArgumentIterator(argument);
			} else {
				logger.fine(party + ": unable to develop argument for " + this.inspect() + ".");
				return new EmptyArgumentIterator();
			}
		// next allow a variable in the left-hand side to be resolved with a number in the right.
		} else if ((this.getLeft() instanceof Variable) && (this.getRight() instanceof ConstantNumber)) {
			subs.add((Variable) getLeft(), getRight());
			Constant unified = this.apply(subs);
			Rule topRule = new Rule(unified);
			topRule.setKnowledgeBase(this.getKnowledgeBase());
			RuleArgument argument = new RuleArgument(topRule, new Double(1.0), subs, new RuleArgumentList(), party, level, d_top, valuator, restrictedRebutting);
			logger.fine(party + ": found " + argument.getName() + " with substitution " + subs.inspect() + ": " + argument.inspect());
			return new SingleArgumentIterator(argument);
		} else {
			// drop back to Term's argumentIterator, just in case.
			return super.argumentIterator(needed, party, level, d_top, valuator, restrictedRebutting);
		}
	}
}
