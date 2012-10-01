package org.aspic.inference;

import org.aspic.inference.writers.ReasonerWriter;


/**
 * A Reasoner Pair is a pair ArgumentLists, PRO and OPP.
 * @author mjs (matthew.south@cancer.org.uk)
 *
 */
public class ReasonerPair {
	private RuleArgumentList proList;
	private RuleArgumentList oppList;

	public ReasonerPair() {
		this.proList = new RuleArgumentList();
		this.oppList = new RuleArgumentList();
	}
	
	public ReasonerPair(RuleArgumentList proList, RuleArgumentList oppList) {
		this.proList = proList;
		this.oppList = oppList;
	}
	
	/**
	 * Get's the Proponent list
	 * @return list of proponent arguments
	 */
	public RuleArgumentList getPRO() { 
		return proList;
	}

	/**
	 * Get's the Opponent list
	 * @return list of opponent arguments
	 */
	public RuleArgumentList getOPP() { 
		return oppList;
	}
	
	/**
	 * text version of object. For example:
	 * <pre>{[Arg1, Arg3],[Arg2]}</pre>
	 * @return view of object
	 */
	public String inspect() {
		return "{" + proList.inspect() + "," + oppList.inspect() +"}";
	}
	/*
	public ReasonerPair copy() {
		return new ReasonerPair((ArgumentList) proList.clone(), (ArgumentList) oppList.clone());
	}
	*/
	
	/**
	 * Allow ReasonerWriter access to this object
	 * @param writer The ReasonerWriter that will do the writing.
	 */
	public void write(ReasonerWriter writer) {
		writer.write(this);
	}
}

