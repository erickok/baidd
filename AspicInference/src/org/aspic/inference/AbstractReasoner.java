package org.aspic.inference;

import java.util.List;



/**
 * Abstract Reasoner.  An Abstract Reasoner Factory is implemented
 * in the Reasoner enum.  If you implement a new Reasoner, be sure
 * to add it to the list of known Reasoners.
 * 
 * @author mjs (matthew.south@cancer.org.uk)
 */
abstract class AbstractReasoner {
	
	/**
	 * A defeasible reasoner starts with a non-conflicting
	 * set of arguments (the pro-list) and recursively builds up the
	 * attacking opp-list and the pro-list until it has reached a stable
	 * conclusion, which it returns.  This conclusion may be a pair of 
	 * empty lists, which indicates that the original pro-list could
	 * not be supported under the implemented semantics.
	 * 
	 * @param testPair ReasonerPair that contains the proList to be evaluated (assumed non conflicting).
	 * @param proof An ArrayList of Arguments that tracks the Arguments developed during the Reasoning process
	 * @return ReasonerPair with either an empty proList and oppList combination or a proof for the support of your original arguments. 
	 */
	abstract ReasonerPair evaluate(ReasonerPair testPair, List<RuleArgument> proof);
}
