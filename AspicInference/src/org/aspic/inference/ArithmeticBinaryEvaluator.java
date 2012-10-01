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
public abstract class ArithmeticBinaryEvaluator extends Binary {
	private static Logger logger = Logger.getLogger(ArithmeticBinaryEvaluator.class.getName());		

	/**
	 * Typical constructor.
	 * @param operator binary built-in predicate's functor.
	 * @param infix flag for "inspect using infix notation".
	 * @param left left operand.
	 * @param right right operand.
	 */
	public ArithmeticBinaryEvaluator(String operator, boolean infix, Element left, Element right) {
		super(operator, infix, left, right);
	}
	
	/**
	 * Default constructor.
	 */
	public ArithmeticBinaryEvaluator(){
		super();
	}		
		

	Iterator<RuleArgument> argumentIterator(Double needed, Party party, int level, int d_top, RuleArgumentValuator valuator, boolean restrictedRebutting) {
		Substitution subsLeft = new Substitution();
		Substitution subsRight = new Substitution();
		// these apply calls are used to evaluate numerical expressions.
		Element candidateLeft = this.getLeft().apply(subsLeft); 
		Element candidateRight = this.getRight().apply(subsRight);
		// Can only compare two numbers
		if ((candidateLeft instanceof ConstantNumber) && (candidateRight instanceof ConstantNumber)) {
			// check that argument is valid.
			if (evaluate((ConstantNumber) candidateLeft, (ConstantNumber) candidateRight)==true) {
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
		} else {
			// drop back to Term's argumentIterator, just in case.
			return super.argumentIterator(needed, party, level, d_top, valuator, restrictedRebutting);
		}
	}
	
	protected abstract boolean evaluate(ConstantNumber left, ConstantNumber right);
}
