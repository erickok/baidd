package org.aspic.inference;

import java.io.Serializable;
import java.util.Iterator;

/**
 * Valuator for List link valuation.
 * @author mjs (matthew.south @ cancer.org.uk)
 *
 */
class LastLinkValuator implements RuleArgumentValuator, Serializable {

	LastLinkValuator() {}
	
	public Double valuate(RuleArgument argument) {
		return (argument.getTopRule().getDob()<1.0) ? 
				argument.getTopRule().getDob() : 
				argument.getSubArgumentList().valuate(this);
	}

	public Double valuate(RuleArgumentList argumentList) {
		Iterator<RuleArgument> itr = argumentList.getArguments().iterator();
		Double min = 1.0;
		while (itr.hasNext()) {
			Double valuation = itr.next().valuate(this);
			if (valuation<min) {
				min = valuation;
			}
		}
		return min;
	}
}
