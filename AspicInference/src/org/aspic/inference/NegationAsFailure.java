package org.aspic.inference;

import java.util.Iterator;
import java.util.logging.Logger;

/**
 * Negation as failure unary operator.  Generates an atomic argument for the
 * operand expression if it cannot generate an argument from the assoicated
 * knowledge base.
 * 
 * @author mjs (matthew.south @ cancer.org.uk)
 */ 
public class NegationAsFailure extends Unary {
	/* 
	 * NB swi-prolog throws an "Undefined Procedure" exception if it tries to evaluate an
	 * \+ expression on a predicate that it doesnt know about.   Do this too?
	 */
	private static Logger logger = Logger.getLogger(NegationAsFailure.class.getName());		
	public NegationAsFailure(Element element) {
		super("\\+", element);
	}
	
	public Constant apply(Substitution subs) {
		if (this.isGrounded()) { 
			return this; 
		} else {
			Term newInstance = new NegationAsFailure(this.getOperand().apply(subs));
			newInstance.setKnowledgeBase(this.getKnowledgeBase());
			return newInstance;
		}
	}
	Iterator<RuleArgument> argumentIterator(Double needed, Party party, int level, int d_top, RuleArgumentValuator valuator, boolean restrictedRebutting) {
		if (getOperand() instanceof Variable) {
			logger.fine(party + ": unable to develop argument for " + this.inspect() + ".");
			return new EmptyArgumentIterator();
		} else 	if (((Constant) this.getOperand()).argumentIterator(needed, party, level, d_top, valuator, restrictedRebutting).hasNext()) {
			return new EmptyArgumentIterator();
		} else {
			Rule topRule = new Rule(NegationAsFailure.this);
			topRule.setKnowledgeBase(this.getKnowledgeBase());
			RuleArgument argument = new RuleArgument(topRule, new Double(1.0), new Substitution(), new RuleArgumentList(), party, level, d_top, valuator, restrictedRebutting);
			logger.fine(party + ": found " + argument.getName() + " : " + argument.inspect());
			return new SingleArgumentIterator(argument);
		}
	}	
}
