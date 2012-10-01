package org.aspic.inference;



/**
 * Enumeration of known Reasoners, with a createReasoner() factory method built in.
 * 
 * @author mjs (matthew.south@cancer.org.uk)
 */
public enum Reasoner {
	/** Grounded semantics **/
	GROUNDED { public GroundedReasoner createReasoner() { return new GroundedReasoner(); }},
	/** Preferred credulous semantics **/
	PREFERRED_CREDULOUS { public PreferredCredulousReasoner createReasoner() { return new PreferredCredulousReasoner(); }};
	
	/** A factory method for the selected Reasoner. **/
	abstract AbstractReasoner createReasoner(); 
}
