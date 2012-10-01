package org.aspic.inference;

import java.util.Iterator;
import java.util.logging.Logger;


/**
 * <p>Abstract class for binary built-in predicates that attempt to 
 * compute both arguments before attempting to generate an Argument.
 * cf <code>Is</code> which is a binary operator that only attempts
 * to compute the right hand side.</p>
 * 
 * @author mjs (matthew.south @ cancer.org.uk)
 *
 */
public abstract class BinaryEvaluator extends Binary {
	private static Logger logger = Logger.getLogger(BinaryEvaluator.class.getName());		

	/**
	 * Typical constructor.
	 * @param operator binary built-in predicate's functor.
	 * @param infix flag for "inspect using infix notation".
	 * @param left left operand.
	 * @param right right operand.
	 */
	public BinaryEvaluator(String operator, boolean infix, Element left, Element right) {
		super(operator, infix, left, right);
	}
	
	/**
	 * Default constructor.
	 */
	public BinaryEvaluator(){
		super();
	}		
		

	Iterator<RuleArgument> argumentIterator(Double needed, Party party, int level, int d_top, RuleArgumentValuator valuator, boolean restrictedRebutting) {
		Substitution subsLeft = new Substitution();
		Substitution subsRight = new Substitution();
		// these apply calls are used to evaluate numerical expressions.
		Element candidateLeft = this.getLeft().apply(subsLeft); 
		Element candidateRight = this.getRight().apply(subsRight);
		// check that argument is valid.
		if (evaluate(candidateLeft, candidateRight)==true) {
			Rule topRule = new Rule(this);
			topRule.setKnowledgeBase(this.getKnowledgeBase());
			Substitution subs = subsLeft.compose(subsRight);
			RuleArgument argument = new RuleArgument(topRule, new Double(1.0), subs, new RuleArgumentList(), party, level, d_top, valuator, restrictedRebutting);
			logger.fine(party + " : found " + argument.getName() + " : " + argument.inspect());
			return new SingleArgumentIterator(argument);
		} else {
			logger.fine(party + ": unable to develop argument for " + this.inspect() + ".");
			return new EmptyArgumentIterator();
		}
	}
	
	protected abstract boolean evaluate(Element left, Element right);
}
