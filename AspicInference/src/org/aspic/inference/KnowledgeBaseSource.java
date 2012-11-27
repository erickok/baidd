package org.aspic.inference;

import java.util.Iterator;

/**
 * An argument source that allows arguments to be accessed from a KnowledgeBase.
 * 
 * @author mjs (matthew.south @ cancer.org.uk)
 *
 */
public class KnowledgeBaseSource implements Cloneable {
/* 
 * At the moment the arguments that are developed from KnowledgeBase
 * are responsible for providing the valuation and interactions so you 
 * have to provide the parameters to tell them what you want.
 * I think this should all be refactored so that the arguments that
 * are devloped here are simpler (unvaluated argument trees) that can
 * be decorated in a pipeline by other Valuator and Interactor 
 * ArgumentSource decorators.  However, I havent managed to implement this yet - MJS.
 */
	
	private KnowledgeBase kb;
	private Valuator valuatorType = Valuator.WEAKEST_LINK;
	private RuleArgumentValuator valuator = Valuator.WEAKEST_LINK.createValuator();
	private boolean restrictedRebutting = false;
	
	/**
	 * Default Constructor.
	 *
	 */
	public KnowledgeBaseSource() {}
	
	/**
	 * Typical constructor.
	 * @param kb knowledge base that will be used to develop arguments.
	 */
	public KnowledgeBaseSource(KnowledgeBase kb) {
		this.kb = kb;
	}
	
	/**
	 * Get all arguments.
	 * @return an iterator over all available arguments.
	 */
	public Iterator<RuleArgument> argumentIterator() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Get all arguments with a particular claim.
	 * @param claim constant to be matched.
	 * @return an iterator over all arguments whose claim matches the passed claim.
	 */
	public Iterator<RuleArgument> argumentIterator(Constant claim) {
		if (claim.getKnowledgeBase()==null) {
			claim.setKnowledgeBase(kb);
		} else if (!claim.getKnowledgeBase().equals(kb)) {
			throw new RuntimeException("Incompatible knowledge bases. The passed claim is already associated with a different knowledge base than this one.");
		}
		return claim.argumentIterator(0.0, Party.PRO, 0, 0, valuator, restrictedRebutting);
	}

	/**
	 * Get all rule argument list permutations whose arguments match a given list of claims.
	 * @param claims list of claims.
	 * @return an iterator overa ll argument lists whose arguments match the list of claims.
	 */
	public Iterator<RuleArgumentList> argumentIterator(ConstantList claims) {
		if (claims.getKnowledgeBase()==null) {
			claims.setKnowledgeBase(kb);
		} else if (!claims.getKnowledgeBase().equals(kb)) {
			throw new RuntimeException("Incompatible knowledge bases. The passed claim is already associated with a different knowledge base than this one.");
		}
		return claims.argumentIterator(0.0, Party.PRO, 0, 0, valuator, restrictedRebutting);
	}

	/** 
	 * Getter for encapsulated knowledge base.
	 * @return reference to current knowledge base.
	 */
	public KnowledgeBase getKnowledgeBase() {
		return kb;
	}

	/**
	 * Setter for encapsulated knoweldge base.
	 * @param kb new knowledge base.
	 */
	public void setKnowledgeBase(KnowledgeBase kb) {
		this.kb = kb;
	}
	
	/**
	 * Getter for valuator.  The valuator defines how arguments are valuated.
	 * @return valuator currently used.
	 */
	public Valuator getValuator() {
		return valuatorType;
	}
	
	/**
	 * Setter for valuator.  The valuator defines how arguments are valuated.
	 * @param valuatorType valuator to be used.
	 */
	public void setValuator(Valuator valuatorType) {
		this.valuatorType = valuatorType;
		valuator = this.valuatorType.createValuator();
	}
	
	/**
	 * Getter for restricted rebutting flag.  Restriced rebutting stops defeasible arguments rebutting strict arguments, no matter how much support they have.
	 * @return boolean flag for restricted rebutting.
	 */
	public boolean isRebuttingRestricted() { 
		return this.restrictedRebutting;
	}
	 /**
	  * Setter for restricted rebutting flag.  Restriced rebutting stops defeasible arguments rebutting strict arguments, no matter how much support they have.
	  * @param restricted boolean flag indicating if restricted rebutting is used.
	  */
	public void setRebuttingRestricted(boolean restricted) {
		this.restrictedRebutting=restricted;
	}

	/**
	 * @deprecated Warning: leaks memory!
	 */
	public Object clone() {
		KnowledgeBaseSource o = null;
		try {
			o = (KnowledgeBaseSource) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		o.kb = (KnowledgeBase) o.getKnowledgeBase().clone();
		return o;
	}
}
