package org.aspic.inference;

/**
 * An interface for valuating rule arguments.
 * 
 * @author mjs (matthew.south @ cancer.org.uk)
 *
 */
public interface RuleArgumentValuator {
	/**
	 * Valuate a single RuleArgument.
	 * @param argument RuleArgument to valuate.
	 * @return valuation.
	 */
	public Double valuate(RuleArgument argument);
	/**
	 * Valuate a RuleArgumentList.
	 * @param argumentList RuleArgumentList to valuate.
	 * @return valuation.
	 */
	public Double valuate(RuleArgumentList argumentList);
}
