package org.aspic.inference;

import java.util.Iterator;
import java.util.logging.Logger;

/**
 * The "nonvar" unary built-in predicate.  This predicate generates 
 * an argument if it's operand is not a variable.
 * @author mjs (matthew.south @ cancer.org.uk)
 */
public class Nonvar extends Unary {
	private static Logger logger = Logger.getLogger(Nonvar.class.getName());		
	private static final String OPERATOR = "nonvar";
	
	/**
	 * Default constructor.
	 */
	public Nonvar(){
		super();
	}
	
	/**
	 * Typical constructor.
	 * @param operand unary operand.
	 */
	public Nonvar(Element operand) {
		super(OPERATOR, operand);
	}
	public Constant apply(Substitution subs) {
		if (this.isGrounded()) { 
			return this; 
		} else {
			Nonvar newNonvar = new Nonvar(getOperand().apply(subs));
			newNonvar.setKnowledgeBase(this.getKnowledgeBase());
			return newNonvar;
		}
	}
	Iterator<RuleArgument> argumentIterator(Double needed, Party party, int level, int d_top, RuleArgumentValuator valuator, boolean restrictedRebutting) {
		if (getOperand() instanceof Variable) {
			logger.fine(party + ": unable to develop argument for " + this.inspect() + ".");
			return new EmptyArgumentIterator();
		} else {
			Rule topRule = new Rule(Nonvar.this);
			topRule.setKnowledgeBase(this.getKnowledgeBase());
			RuleArgument argument = new RuleArgument(topRule, new Double(1.0), new Substitution(), new RuleArgumentList(), party, level, d_top, valuator, restrictedRebutting);
			logger.fine(party + ": found " + argument.getName() + " : " + argument.inspect());
			return new SingleArgumentIterator(argument);
		}
	}	
}
