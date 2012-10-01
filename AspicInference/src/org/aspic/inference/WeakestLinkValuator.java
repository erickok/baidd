package org.aspic.inference;

import java.io.Serializable;
import java.util.Iterator;

/**
 * Valuator for weakest link valuation.
 * @author mjs (matthew.south @ cancer.org.uk)
 *
 */
class WeakestLinkValuator implements RuleArgumentValuator, Serializable {

	WeakestLinkValuator() {}
	
	public Double valuate(RuleArgument argument) {
		return Math.min(argument.getTopRule().getDob(), 
				(argument.getSubArgumentList()!=null ? argument.getSubArgumentList().valuate(this) : 1.0));
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
