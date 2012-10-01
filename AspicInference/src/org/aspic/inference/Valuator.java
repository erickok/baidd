package org.aspic.inference;


/**
 * Enumeration of known Valuators, with a createValuator() factory method built in.
 * 
 * @author mjs (matthew.south@cancer.org.uk)
 */
public enum Valuator {
	/** Weakest link valuation - the minimum of all sub Argument's valuation **/
	WEAKEST_LINK { public RuleArgumentValuator createValuator() { return new WeakestLinkValuator(); }},
	/** Last link valuation - the degree of belief of the shallowest rule. If there is a choice of rules, min is used between them. **/
	LAST_LINK { public RuleArgumentValuator createValuator() { return new LastLinkValuator(); }};
	
	/** factory method for Valuator object **/
	public abstract RuleArgumentValuator createValuator(); 
}
