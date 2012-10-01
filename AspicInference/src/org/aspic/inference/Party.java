package org.aspic.inference;

/**
 * Enum representing the two parties in a dialectical proof.
 * This enum is used log.info messages, to help a user trace the
 * reasoning process.
 * 
 * @author mjs (matthew.south @ cancer.org.uk)
 */
public enum Party {
	/** Proponent. Sometimes called constructor or denoted by IN. **/
	PRO,
	/** Opponent. Sometimes called destructor or denoted by OUT. **/	
	OPP;
}

