package org.aspic.inference;

import java.util.List;



/** 
 * Abstract class for a built-in predicate.  A built-in predicate 
 * is a Term with extra semantics that may or may not produce a single argument.
 * Arithmetic operators and standard prolog interrogative operators 
 * (such as "is/2" or "nonvar/1" or "constant/1") are defined by 
 * inheriting from this class.
 * @author mjs (matthew.south @ cancer.org.uk)
 */
public abstract class BuiltIn extends Term {
	
	/**
	 * Default constructor.
	 */
	public BuiltIn(){
		super();
	}
	
	/**
	 * Typical constructor.
	 * @param operator the built-in predicate's functor.
	 * @param argList the list of this built-in predicate's arguments.
	 */
	public BuiltIn(String operator, ElementList argList){
		super(operator, argList);
	}

	@Override
	List<Predicate> getPredicates() {
		return super.getArgList().getPredicates(); // don't return the functor for built ins
	}
}
