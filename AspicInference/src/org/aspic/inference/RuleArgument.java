package org.aspic.inference;

import org.aspic.inference.writers.ReasonerWriter;

import java.io.Serializable;
import java.util.*;
import java.util.logging.Logger;

/**
 * <p>
 * Class for representing Arguments.   An argument is associated with
 * a rule, it's top rule.  If that rule has premises then the argument 
 * will have sub-arguments.  If that rule doesnt have any premises
 * then the argument will be atomic.  An argument's 
 * conclusion is the instantiated version of it's top rule's consequent.
 * A main argument is the argument at the top of a tree (with level 0).
 * A main argument's conclusion is grounded (i.e. it contains no 
 * variables), but the conclusions of sub-arguments may contain free variables.
 * </p>
 * <p>
 * Arguments are generated in the Rule class', argumentIterator().
 * During reasoning, the question is asked: what are the arguments
 * for some claim, or for the negation of that claim.  The claim 
 * is a Constant or Term and the argumentIterator() method on those
 * classes calls the argumentIterator() method on the Rule class.
 * </p>
 * <p>
 * There are several Lists of Arguments associated with this class.
 * Attacking arguments are all arguments that are known to attack
 * (i.e. contradict) the conclusion of this argument. <em>Successful</em>
 * attacking arguments are those attacking arguments that are
 * strong enough to defeat this argument.  <em>Deep</em> successful
 * attacking arguments are those arguments that successfully attack
 * this argument or any of it's sub arguments.</p>
 * 
 * <p>ASPIC rebuts and undercuts are not explicitly recognised in this class
 * and must be identified by examining the target of an attack.
 * In particular, an attack that's directed at an atomic argument whose conclusion
 * is a rule name, is an undercutting attack, else it's
 * a rebutting attack.
 * </p>
 * TODO (later): consider making a lot of this stuff "final" for performance optimisation 
 * @author mjs (matthew.south @ cancer.org.uk)
 * */
public class RuleArgument implements Serializable {
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(Rule.class.getName());
	/**
	 * Enumeration of possible Argument status values, as defined within the context of a particular Query.
	 * @author mjs (matthew.south @ cancer.org.uk)
	 */
	public enum Status {DEFEATED, UNDEFEATED}	
	private static int counter = 0;	
	
	private Rule topRule;
	private Double modifier;
	private Substitution substitution; 
	private RuleArgumentList subArgumentList;
	private Party owner;
	private int level;
	private int d_top;
	private RuleArgumentValuator valuator;
	private boolean restrictedRebutting;
	private Status status;
	private int number;
	private int parentNumber;

	private List<RuleArgument> successfulAttackerCache = new ArrayList<RuleArgument>();
	private List<RuleArgument> unsuccessfulAttackerCache = new ArrayList<RuleArgument>();
	// private Map<Scheme, List<SchemeApplication<Constant, Double>>> linkedSchemes = new HashMap<Scheme, List<SchemeApplication<Constant, Double>>>();
	// NB the question of whether the value in the linkedSchemes should be a List or not is still open.  It depends on whether you woudl want to use two different SchemeApplication obejcts for the same, (probably non inference), scheme.
	
	/**
	 * Argument construction is recursive.  To generate an argument
	 * you first generate it's sub arguments (and so on...).
	 * 
	 * @param topRule the rule used to support this argument's conclusion
	 * @param modifier the support for the argument
	 * @param substitution the substitution used to develop this argument
	 * @param subArguments the argumentlist that supports the antecedent of the top rule
	 * @param party the reasoner party constructing this argument (useful for logging info)
	 * @param level the current depth of recursion
	 * @param d_top the distance from the top .
	 * @param valuator the valuator used when generating attacking arguments.
	 * @param restrictedRebutting a flag for controlling allowed successful attacks.
	 */
	public RuleArgument(Rule topRule, Double modifier, Substitution substitution, RuleArgumentList subArguments, Party party, int level, int d_top, RuleArgumentValuator valuator, boolean restrictedRebutting) {
		// NB d_top used for isMainArgument, level used for pretty printing log messages
		this.topRule = topRule;
		this.modifier = modifier;
		this.substitution = substitution;
		this.subArgumentList = subArguments;
		this.owner = party;
		this.level = level;
		this.d_top = d_top;
		this.valuator = valuator;
		this.restrictedRebutting = restrictedRebutting;
		this.number = ++counter;
		Iterator<RuleArgument> subArgsIterator = subArguments.getArguments().iterator();
		while (subArgsIterator.hasNext()) {
			subArgsIterator.next().parentNumber = this.number;
		}
/*		SchemeApplication<Constant, Double> inferenceSchemeApplication =  new SchemeApplication(Scheme.getScheme("dMP"), subArguments.getArguments());
		addSchemeApplication(inferenceSchemeApplication); */
	}

	/* Methods */

	/**  
	 * Two Arguments are semantically equal iff their conclusions are equal modulo 
	 * variable names and the same holds for their subarguments.
	 */
	public boolean isSemanticallyEqual(RuleArgument candidate) {
		if (this.getClaim().isEqualModuloVariables(candidate.getClaim())) {
			if (this.subArgumentList.getArguments().size()==candidate.subArgumentList.getArguments().size()) {
				boolean tentativeResult = true;
				for (int i=0; i<this.subArgumentList.getArguments().size(); i++) {
					if (!this.subArgumentList.getArguments().get(i).isSemanticallyEqual(candidate.subArgumentList.getArguments().get(i))) {
						tentativeResult = false;
					}
				}
				return tentativeResult;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	
	public boolean isUnifiable(RuleArgument candidate) {
		if (this.getClaim().isUnifiable(candidate.getClaim())) {
			if (this.subArgumentList.getArguments().size()==candidate.subArgumentList.getArguments().size()) {
				for (RuleArgument subArgument : this.subArgumentList.getArguments()) {
					if (!candidate.subArgumentList.includesUnifiable(subArgument)) return false;
				}
				return true;
			} else {
				return false;
			}
			
		} else return false;
	}

	/**
	 * Checks this argument against a supposed attacker and returns true if this argument has equal or greater support than the attacker.
	 * @param attacker argument to check
	 * @return true if this argument is at least as strong as the attacker (based on support)
	 */
	boolean isAsStrongAs(RuleArgument attacker) {
		return this.getModifier() >= attacker.getModifier();
	}
	
	/**
	 * Checks whether this Argument succesfully attacks the proposed victim argument's main argument.
	 * Under restricted rebutting, an argument whose top rule is defeasible cannot succesfully attack
	 * an argument whose top rule is strict.
	 * @param test argument to be checked against this one
	 * @return true if this argument's conclusion contradicts the test argument's conclusion and this argument is as strong as the test argument
	 */
	boolean isSuccessfulAttackerOf(RuleArgument test) {
		return (test.getModifier()<1.0) && !(restrictedRebutting && !this.getTopRule().isStrict() && test.getTopRule().isStrict()) &&
			this.getClaim().negation().isEqualModuloVariables(test.getClaim()) && 
				this.isAsStrongAs(test);
	}
	
	/** 
	 * Checks whether this argument succesfully attacks the proposed victim argument 
	 * (or any of it's sub-arguments).
	 * @param victim argument that might be attacked
	 * @return true if victim or any of it's sub-arguments is attacked by this argument.
	 */
	boolean isDeepSuccessfulAttackerOf(RuleArgument victim) {
		Iterator<RuleArgument> victimSubArgumentIterator = victim.subArgumentIterator();
		while (victimSubArgumentIterator.hasNext()) {
			RuleArgument subarg = victimSubArgumentIterator.next();
			if (this.isSuccessfulAttackerOf(subarg)) return true;
		}
		return false;
	}

	/**
	 * Yield all arguments against the conclusion of this argument
	 * @param party PRO or OPP: which party is using this argument
	 * @param level used for tracking depth of recursion
	 * @return Argument Iterator for all attacking arguments
	 */
	Iterator<RuleArgument> attackingArgumentIterator(final Party party, final int level) {
		return getClaim().negation().argumentIterator(0.0, party, level, 0, valuator, restrictedRebutting);
	}
	
	
	/**
	 * Yield all attacking arguments that are strong enough to defeat 
	 * this argument.
	 * @param party PRO or OPP: which party is using this argument
	 * @param level used for tracking depth of recursion
	 * @return Argument Iterator for all successful attacking arguments
	 */
	Iterator<RuleArgument> successfulAttackingArgumentIterator(Party party, int level) {
		return new SuccessfulAttackingArgumentIterator(party, level);
	}
	
	/**
	 * Yield all successful attacking arguments for this argument and all it's
	 * sub arguments.
	 * @param party PRO or OPP: which party is using this argument
	 * @param level used for tracking depth of recursion
	 * @return Argument Iterator for all defeating arguments
	 */
	Iterator<RuleArgument> deepSuccessfulAttackingArgumentIterator(Party party, int level) {
		return new DeepSuccessfulAttackingArgumentIterator(party, level);
	}
	
	/**
	 * On discovery, unsucessful attacking arguments are placed in a cache for
	 * later inspection.
	 * @return cache of unsuccessful attacking arguments.
	 */
	public List<RuleArgument> getUnsuccessfulAttackerCache() {
		return unsuccessfulAttackerCache;
	}
	
	/**
	 * On discovery, successful attacking arguments are placed in a cache for
	 * later inspection.
	 * @return cache of successful attacking arguments.
	 */
	public List<RuleArgument> getSuccessfulAttackerCache() {
		return successfulAttackerCache;
	}
	
	/**
	 * Useful for drawing proof diagrams.  If you know 
	 * the parent of this argument, and you've determined 
	 * that a particular attack is an undercut, then you
	 * can direct that attack at the inference scheme application
	 * node that joins this argument node to it's parent.
	 * @return parent argument's number or null if this argument is a main argument
	 */
	public int getParentNumber() {
		return parentNumber;
	}

	/**
	 * Gets the sub argument list.
	 * @return ArgumentList containing sub arguments and the substitution used to develop those arguments.
	 */
	public RuleArgumentList getSubArgumentList() {
		return subArgumentList;
	}

	/**
	 * Support is the belief of the conclusion.
	 * @return support for conclusion.
	 */
	public Double getModifier() {
		return modifier;
	}

	/**
	 * Get's reference to the top rule.
	 * @return reference to argument's top rule.
	 */
	public Rule getTopRule() {
		return topRule;
	}
	
	/**
	 * Gets the argument's (locally) unique number.
	 * @return the number uniquely identifying the argument.
	 */
	public int getNumber() {
		return number;
	}
	
	/**
	 * Gets the substution used to make the argument.
	 * @return substitution used for argument
	 */
	public Substitution getSubstitution() {
		return substitution;
	}

	/**
	 * Gets the status of the argument (if set).
	 * @return the argument's status
	 */
	public Status getStatus() {
		return status;
	}
	
	/**
	 * Sets the status of the argument.
	 * @param status
	 */
	public void setStatus(Status status) {
		this.status = status;
	}
	
	/* Virtual Getters */
	
	/**
	 * Virtual getter. Returns "Arg" + <code>number</code>.
	 * @return Argument's unique name
	 */
	public String getName() {
		return "Arg" + number;
	}

	/**
	 * Virtual getter. Checks if the argument has sub arguments.
	 * @return true if argument doesnt have sub arguments	 
	 */
	public boolean isAtomic() {
		return (subArgumentList.getArguments().size()==0);
	}

	/**
	 * Virtual getter. Based on argument's support and structure.
	 * @return true if argument has support of 1.0 and is atomic
	 */
	public boolean isFactual() {
		return (modifier==1.0) && isAtomic();
	}
	
	/**
	 * Virtual getter. Checks whether the argument is at the top of a tree.
	 * @return true if the argument is at the top of it's tree.
	 */
	public boolean isMainArgument() {
		return (d_top==0);
	}
	
	/**
	 * Virtual getter. Get's the conclusion of the argument.
	 * @return consequent of top rule with substitution applied.
	 */
	public Constant getClaim() {
		return topRule.getConsequent();
	}
	
	/* Concrete Getters */
	
	/*
	 * Get's the distance from the top of the argument
	 * used in isMain
	public int getD_top() {
		return d_top;
	}
	 */

	/*
	public int getLevel() {
		return level;
	}
*/
	/*
	public Party getOwner() {
		return owner;
	}*/
/*
	public ArgumentList getSubArguments() {
		return subArguments;
	}
*/	
	/**
	 * Accesses all sub arguments for this argument (including itself).
	 * @return iterator over sub arguments
	 */
	public Iterator<RuleArgument> subArgumentIterator() {
		return new SubArgumentIterator();
	}
	
	/*
	 * Get's number of sub arguments.
	 * @return size of sub argument list.
	public int getNumberOfSubArguments() {
		// TODO?: deprecate in favour of .getSubArgumentList.getArguments().size()
		return subArgumentList.getArguments().size();
	}
	 */
	
	/**
	 * Reset internal counter that assigns argument numbers.
	 * Useful for Testing.
	 */
	public static void resetArgCounter() {
		counter = 0;
	}
	
	/**
	 * A default way of inspecting the argument. e.g.
	 * <pre>
	 * a (0.2) <~ (0.2) 
	 * 		b (0.4)
	 * 		c (0.5)
	 * </pre>	
	 * @return String representation of argument
	 */
	public String inspect() {
		return inspect(0);
	}
	/** 
	 * An overloaded version of <code>inspect</code> that provides control over indentation.
	 * @param level used to indicate level of nesting.
	 * @return indented String representation of this Argument. 
	 */
	public String inspect(int level) {
		String result = repeat("  ", level) + this.getClaim().inspect() + " " + this.getModifier();
		if (this.subArgumentList.getArguments().size()>0) {
			result += this.topRule.isStrict() ? " <-\n" : " <~ (" + this.topRule.getDob() + ")\n"; 
			Iterator<RuleArgument> subargIterator = this.subArgumentList.getArguments().iterator();
			while (subargIterator.hasNext()) {
				result += subargIterator.next().inspect(level+1);
			}
		} else {
			result += "\n";
		}
		return result;
	}
	
	public void write(ReasonerWriter writer) {
		writer.write(this);
	}
	
	public String toString() {
		return this.getName() + " : " + this.getClaim().inspect() + " : " + this.getModifier();
	}
	
	Double valuate(RuleArgumentValuator valuator) {
		return valuator.valuate(this);
	}

	/** 
	 * Little helper function. Reuturns the <code>repeated</code> x <code>numtimes</code>.
	 * @param repeated the string to be repeated
	 * @param numtimes the number of times it should be repeated
	 * @return the <code>repeated</code> string, repeated <code>numtimes</code> times.
	 */
	private String repeat(String repeated, int numtimes) {
		StringBuffer result = new StringBuffer("");
		for (int i=0; i<numtimes; i++) {
			result.append(repeated);
		}
		return result.toString();
	}
	/*
	@Override
	public List<SchemeApplication<Constant, Double>> linkedSchemes() {
		List<SchemeApplication<Constant, Double>> list = new ArrayList<SchemeApplication<Constant, Double>>();
		Iterator<Scheme> schemeIterator = linkedSchemes.keySet().iterator();
		while(schemeIterator.hasNext()) {
			list.addAll(linkedSchemes.get(schemeIterator.next()));
		}
		return list;
	}

	@Override
	public List<SchemeApplication<Constant, Double>> linkedSchemes(Scheme[] recurseSchemes) {
		Arrays.sort(recurseSchemes); // make pre-condidition?
		List<SchemeApplication<Constant, Double>> list = new ArrayList<SchemeApplication<Constant, Double>>();
		Iterator<Scheme> schemeIterator = linkedSchemes.keySet().iterator();
		while(schemeIterator.hasNext()) {
			Scheme scheme = schemeIterator.next();
			list.addAll(linkedSchemes.get(scheme));
			if (Arrays.binarySearch(recurseSchemes, scheme) > -1) {
				Iterator<SchemeApplication<Constant, Double>> itr = linkedSchemes.get(scheme).iterator();
				while (itr.hasNext()) {
					SchemeApplication sa = itr.next();
					Iterator<InferenceArgument> itrArgs = sa.getLinkedArguments().iterator();
					while (itrArgs.hasNext()) {
						list.addAll(itrArgs.next().linkedSchemes(recurseSchemes));
					}
				}
			}
		}
		return list;
	}

	@Override
	public List<SchemeApplication<Constant, Double>> linkedFilteredSchemes(Scheme[] filterSchemes) {
		Arrays.sort(filterSchemes); // make pre-condidition?
		List<SchemeApplication<Constant, Double>> list = new ArrayList<SchemeApplication<Constant, Double>>();
		Iterator<Scheme> schemeIterator = linkedSchemes.keySet().iterator();
		while(schemeIterator.hasNext()) {
			Scheme scheme = schemeIterator.next();
			if (Arrays.binarySearch(filterSchemes, scheme) > -1) {
				list.addAll(linkedSchemes.get(scheme));
			}
		}
		return list;
	}

	@Override
	public List<SchemeApplication<Constant, Double>> linkedFilteredSchemes(Scheme[] schemes, Scheme[] recurseSchemes) {
		Arrays.sort(schemes); // make pre-condidition?
		Arrays.sort(recurseSchemes); // make pre-condition?
		List<SchemeApplication<Constant, Double>> list = new ArrayList<SchemeApplication<Constant, Double>>();
		Iterator<Scheme> schemeIterator = linkedSchemes.keySet().iterator();
		while(schemeIterator.hasNext()) {
			Scheme scheme = schemeIterator.next();
			if (Arrays.binarySearch(schemes, scheme) > -1) {
				list.addAll(linkedSchemes.get(scheme));
			}
			if (Arrays.binarySearch(recurseSchemes, scheme) > -1) {
				Iterator<SchemeApplication<Constant, Double>> itr = linkedSchemes.get(scheme).iterator();
				while (itr.hasNext()) {
					SchemeApplication sa = itr.next();
					Iterator<InferenceArgument> itrArgs = sa.getLinkedArguments().iterator();
					while (itrArgs.hasNext()) {
						list.addAll(itrArgs.next().linkedFilteredSchemes(schemes, recurseSchemes));
					}
				}
			}
		}
		return list;
	}

	
	void addSchemeApplication(SchemeApplication<Constant, Double> schemeApplication) {
		if (linkedSchemes.containsKey(schemeApplication.getScheme())) {
			linkedSchemes.get(schemeApplication.getScheme()).add(schemeApplication);
		} else {
			List<SchemeApplication<Constant, Double>> list = new ArrayList<SchemeApplication<Constant, Double>>();
			list.add(schemeApplication);
			linkedSchemes.put(schemeApplication.getScheme(), list);
		}
	}
	*/
	/**
	 * <p>An Iterator that yields all sub arguments of this argument, including itself.</p>
	 * @author mjs (matthew.south @ cancer.org.uk)
	 */
	class SubArgumentIterator implements Iterator<RuleArgument> {
		private boolean dispatchedSelf;
		private Iterator<RuleArgument> subArgumentListIterator = null;
		
		private Iterator<RuleArgument> subArgumentIterator = null;
		private RuleArgument nextArgument = null;
		private boolean queued = false;
		
		public SubArgumentIterator() {
			dispatchedSelf = false;
			subArgumentListIterator = subArgumentList.getArguments().iterator();
			hasNext();
		}
		
		public boolean hasNext() {
		 	if (queued==true) { 
		 		return true;
		 	} else {
		 		if (dispatchedSelf==false) {
		 			nextArgument = RuleArgument.this;
		 			dispatchedSelf=true;
		 			queued=true;
		 			return true;
		 		} else {
		 			if (subArgumentIterator!=null && subArgumentIterator.hasNext()) {
		 				nextArgument = subArgumentIterator.next();
		 				queued=true;
		 				return true;
		 			} else {
		 				if (subArgumentListIterator.hasNext()) {
			 				subArgumentIterator = subArgumentListIterator.next().subArgumentIterator();
			 				return hasNext();		 			
			 			} else {
			 				return false;
		 				}
		 			}
		 		}
		 	}
		}
		
		public RuleArgument next() {
			if (queued) {
				queued=false;
				return nextArgument;
			} else {
				throw new NoSuchElementException();
			}
		}
		
		public void remove() {
			throw new UnsupportedOperationException();			
		}
	}
	
	/**
	 * <p>An Iterator that yields all attacking arguments that can defeat this Argument (taking into account the restrictedRebutting flag).</p>
	 * <p>Assumes user will poll hasNext() before calling next() after the first argument has been returned.</p>  
	 * @author mjs (matthew.south @ cancer.org.uk)
	 */
	class SuccessfulAttackingArgumentIterator implements Iterator<RuleArgument> {
		Party party = null;
		int level=0;
		Iterator<RuleArgument> attackingArgumentIterator;
		boolean queuedArgument = false;
		RuleArgument nextArgument;
		
		public SuccessfulAttackingArgumentIterator(Party party, int level) {
			this.party = party;
			this.level = level;
			attackingArgumentIterator = RuleArgument.this.attackingArgumentIterator(party, level);
			queuedArgument = hasNext();
		}
		
		public boolean hasNext() {
			if (queuedArgument==true) {
				return true;
			} else {				
				while(attackingArgumentIterator.hasNext()) {
					RuleArgument attackingArgument = attackingArgumentIterator.next();
					/*
					if (attackingArgument.isMainArgument() && attackingArgument.getTopRule().getKnowledgeBase().isRuleName(attackingArgument.getTopRule().getConsequent())) {
						// add undercutter interaction
						SchemeApplication<Constant, Double> inferenceSchemeApplication =  new SchemeApplication<Constant, Double>(Scheme.getScheme("uct"), attackingArgument);
						addSchemeApplication(inferenceSchemeApplication);						
					} else { 
						// add rebutter interaction
						SchemeApplication<Constant, Double> inferenceSchemeApplication =  new SchemeApplication<Constant, Double>(Scheme.getScheme("rbt"), attackingArgument);
						addSchemeApplication(inferenceSchemeApplication);						
					}
					*/
					boolean restricted = (restrictedRebutting && !attackingArgument.getTopRule().isStrict() && RuleArgument.this.getTopRule().isStrict());
					if ((RuleArgument.this.getModifier()<1.0) && attackingArgument.isAsStrongAs(RuleArgument.this) && !restricted) { 
						logger.fine(party.toString() + ": " + attackingArgument.getName() + " strong enough to interfere with " + RuleArgument.this.getName());
						successfulAttackerCache.add(attackingArgument);
						// add defeat interaction 
						/*
						SchemeApplication<Constant, Double> inferenceSchemeApplication =  new SchemeApplication<Constant, Double>(Scheme.getScheme("dft"), attackingArgument);
						addSchemeApplication(inferenceSchemeApplication); */						
						nextArgument = attackingArgument;
						queuedArgument = true;
						return true;
					} else {
						logger.fine(party.toString() + ": " + attackingArgument.getName() + " too weak to interfere with " + RuleArgument.this.getName());
						unsuccessfulAttackerCache.add(attackingArgument);
						return hasNext();
					}
				}
				return false;
			}
		}
		
		public RuleArgument next() {
			queuedArgument = false;
			return nextArgument;
		}
		
		public void remove() {
			throw new UnsupportedOperationException();			
		}
	}

	/**
	 * <p>An Iterator that yields all successful attacking Arguments to this
	 * Argument and it's sub-Arguments.</p>
	 * <p>Assumes user will poll hasNext() before calling next() after the first argument has been returned.</p>  
	 * @author mjs (matthew.south @ cancer.org.uk)
	 */
	class DeepSuccessfulAttackingArgumentIterator implements Iterator<RuleArgument> {
		private Party party=null;
		private int level=0;
		
		private Iterator<RuleArgument> subArgIterator;
		private RuleArgument currentSubArg;
		private Iterator<RuleArgument> successfulAttackingArgumentIterator;
		private boolean queuedArgument = false;
		private RuleArgument nextArgument;
		
		public DeepSuccessfulAttackingArgumentIterator(Party party, int level) {
			this.party = party;
			this.level = level;
			subArgIterator = RuleArgument.this.subArgumentIterator();
			queuedArgument = hasNext();
		}
		
		public boolean hasNext() {
			if (queuedArgument==true) {
				return true; // in case someone tries hasNext() twice.
			} else {
				if (successfulAttackingArgumentIterator!=null && successfulAttackingArgumentIterator.hasNext()) {
					nextArgument = successfulAttackingArgumentIterator.next();
					return true;
				} else {
					if (subArgIterator.hasNext()) {
						currentSubArg = subArgIterator.next();
						logger.fine(party.toString() + ": examining " + currentSubArg.getName());
						successfulAttackingArgumentIterator = currentSubArg.successfulAttackingArgumentIterator(party, level);
						return hasNext();
					} else {
						return false;
					}
				}
			}
		}
		
		public RuleArgument next() {
			queuedArgument = false;
			return nextArgument;
		}	
		
		public void remove() {
			throw new UnsupportedOperationException();			
		}		
	}
}
