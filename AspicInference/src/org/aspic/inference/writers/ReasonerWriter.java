package org.aspic.inference.writers;

import org.aspic.inference.*;


/** 
 * A writer interface for all non abstract reasoner classes.
 * 
 * @author mjs (matthew.south@cancer.org.uk)
 *
 */
public interface ReasonerWriter {
	void write(Query query);
	void write(RuleArgument argument);
	void write(RuleArgumentList argumentList);
	void write(Substitution substitution);
	void write(ReasonerPair reasonerPair);
}
