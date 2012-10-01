package org.aspic.inference;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

import org.aspic.inference.writers.KnowledgeWriter;

/**
 * A <code>Constant</code> is the simplest sub-class of <code>Element</code>.
 * It encapsulates a <code>functor</code> that can be used to represent a prolog grounded atom.
 * @author mjs (matthew.south @ cancer.org.uk)
 */
public class Constant extends Element implements Cloneable {
	private static final long serialVersionUID = 1L; // default

	private static Logger logger = Logger.getLogger(Constant.class.getName());		

	private String functor = ""; 
	
	/**
	 * Default Constructor.
	 */
	public Constant() {}
	
	/**
	 * Typical constructor.  A Constant needs a functor.
	 * @param functor
	 */
	public Constant(String functor){
		// automatically quote if you can.
		// NB Doesnt catch constants that start with a number but arent numbers, e.g. "206LX"
		if (functor.length()>0) {
			char first = functor.toCharArray()[0];
			if (functor.contains(" ") || (first>=65 && first <=91)) {
				if (!functor.startsWith("'")) functor = "'" + functor;
				if (!functor.endsWith("'")) functor = functor + "'";
			}
			this.functor=functor;
		} else {
			throw new RuntimeException("Cannot create a functor with an empty String");
		}
	}
	/*
    public String toStringXml(){
        return "<constant><functor>" + getFunctor() + "</functor></constant>";      
    }    
    */
	/**
	 * Simple getter for functor.
	 * @return constant's functor.
	 */
	public String getFunctor() {
		return functor;
	}

	/**
	 * Simple setter of functor.
	 * @param functor new functor.
	 */
	public void setFunctor(String functor) {
		this.functor = functor;
	}

	/**
	 * Get's the negation of this constant, a <code>Term</code> with functor
	 * "~", and parameters (<code>functor</code>).
	 * @return Term representing negation of this Constant
	 */
	public Constant negation() { 
		/* NB Why is this a Constant when it returns a Term?
		 * Because a Term can return a Constant: ~(~(a)) = a */
		ElementList negationArgs = new ElementList(this);
		negationArgs.setKnowledgeBase(this.getKnowledgeBase());
		Term negation = new Term("~", negationArgs);
		negation.setKnowledgeBase(this.getKnowledgeBase());
		return negation;
	}

	public String inspect() {
		// return functor.contains(" ") ? "'" + functor + "'" : functor;
		return functor;
	}
		
	public Constant apply(Substitution subs) {
		return this;
	}
	
	public boolean isUnifiable(Element toUnify) {
		return ((toUnify instanceof Variable) ||
				((toUnify instanceof Constant) && (((Constant) toUnify).getFunctor().equals(this.getFunctor()))));
	}
	
	public Substitution unify(Element toUnify, Substitution subs) {
		if (toUnify instanceof Variable) {
			subs.add((Variable) toUnify, this);
		}
		return subs;	
	}

	public boolean isEqualModuloVariables(Element testClause) {
		// NB Public because it's useful for comparing terms.
		return (testClause instanceof Constant  && !(testClause instanceof Term)) 
				&& (((Constant)testClause).getFunctor().compareTo(this.getFunctor())==0);
	}
	
	public boolean isGrounded() {
		// NB public because it's used in Decision.
		return true;
	}
	
	/**
	 * Used as the key in KnowledgeBase.rulesMap.
	 * @return a string that identifies this constant (or term).
	 */
	public String ruleTag() {
		return functor;
	}

	public void write(KnowledgeWriter writer) {
		writer.write(this);
	}
	
	List<Variable> getVariables() {
		return new ArrayList<Variable>();
	}

	public boolean equals(Object o) {
		return (o instanceof Element) && this.isEqualModuloVariables((Element) o);
	}	

	/*
	public boolean equals(Object o) {
		return (o instanceof Constant) && this.functor.equals(((Constant) o).getFunctor());
	}
	*/
	public int hashCode() {
		return functor.hashCode();
	}
	
	/*
	public String argumentTag(Double needed) {
		return functor + "_" + needed.toString();
	}
	*/
	
	/**
	 * <p>Provide an iterator for all arguments that can be generated for this constant,
	 * with a support that's at least that defined by <code>needed</code>, from the 
	 * Knowledge base that this constant is associated with.</p>
	 * <p>If restricted rebutting is used then arguments whose top rule is defeasible
	 * are prevented from rebutting arguments whose top rule is strict, no matter how
	 * much support they have.</p>
	 * @param needed a threshold of support needed by the sought arguments
	 * @param party the party who's developing the arguments (useful for logging)
	 * @param level the current level of recursion
	 * @param d_top the distance from the top
	 * @param valuator the RuleArgumentValuator used to valuate Arguments
	 * @param restrictedRebutting restricted rebutting flag.
	 * @return Argument Iterator
	 **/
	Iterator<RuleArgument> argumentIterator(Double needed, Party party, int level, int d_top, RuleArgumentValuator valuator, boolean restrictedRebutting) {
		return new ConstantArgumentIterator(needed, party, level, d_top, valuator, restrictedRebutting);
	}

	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
	
	/**
	 * <p>Used by Constant.argumentIterator(...).
	 * The way to provide an Iterator over all arguments for this Constant is to
	 * look at all knowledge base rules with this constant in the consequent and 
	 * then get all the arguments for those rules.
	 * What's tricky about this iterator, is that you never know if the rule you
	 * identify will have any arguments in advance, so you have to "Look ahead"
	 * a little bit in order to supply a decent answer for the "hasNext()" method.</p>
	 * <p>Assumes user will poll hasNext() before calling next() after the first argument has been returned.</p>  
	 * 
	 * @author mjs (matthew.south @ cancer.org.uk)
	 */
	private class ConstantArgumentIterator implements Iterator<RuleArgument> {
		// a container for all rules with this constant in the head, 
		// indexed by arity of rule (0...n).
		private Map<Integer, List<Rule>> arityMap; 

		private Double needed;
		private Party party;
		private int level;
		private int d_top;
		private RuleArgumentValuator valuator;
		private boolean restrictedRebutting;
		
		// used to iterate through the arityMap arities
		private Iterator<Integer> arityIterator;
		// used to iterate through the list of rules for a particular arity
		private Iterator<Rule> ruleIterator; 
		// used to iterate through the arguments provided by a particular rule
		private Iterator<RuleArgument> argumentIterator; 
		
		private boolean queuedArgument = false;
		private RuleArgument nextArgument = null;
		
		public ConstantArgumentIterator(Double needed, Party party, int level, int d_top, RuleArgumentValuator valuator, boolean restrictedRebutting) {
			this.needed = needed;
			this.party = party;
			this.level = level;
			this.d_top = d_top;
			this.valuator = valuator;
			this.restrictedRebutting = restrictedRebutting;
			logger.fine(party.toString() + ": searching for arguments for literal " + Constant.this.inspect() + ((needed>0.0) ? needed : ""));
			// set up your iterators
			this.arityMap = Constant.this.getKnowledgeBase().getArityMap(Constant.this.ruleTag());
			if (arityMap!=null) {	
				arityIterator = arityMap.keySet().iterator();
				ruleIterator = getNextRuleIterator();
				argumentIterator = getNextArgumentIterator();
			}
			queuedArgument = this.hasNext();
		}
		
		public boolean hasNext() {
			if (queuedArgument==true) {
				return true; // in case hasNext is called twice.
			} else {
				if (arityMap!=null) {
					if (argumentIterator!=null && argumentIterator.hasNext()) {
						nextArgument = argumentIterator.next();
						queuedArgument=true;
						logger.fine(party.toString() + ": found " + nextArgument.getName() + " : " + nextArgument.inspect(level));
						return true;
					} else {
						if (ruleIterator!=null && ruleIterator.hasNext()) {
							argumentIterator  = getNextArgumentIterator();
							return this.hasNext();
						} else {
							if (arityIterator.hasNext()) {
								ruleIterator = getNextRuleIterator();
								return this.hasNext();
							} else {
								return false;
							}
						}
					}
				}
				else {
					return false;
				}
			}
		}
		
		public RuleArgument next() {
			if (queuedArgument==true) {
				queuedArgument = false;
				return nextArgument;
			} else {
				throw new NoSuchElementException();
			}
		}
		
		public void remove() {
			throw new UnsupportedOperationException();			
		}
		
		private Iterator<Rule> getNextRuleIterator() {
			if (arityIterator.hasNext()) {
				int arity = arityIterator.next();
				logger.fine(party.toString() + ": found one or more " + (arity>0 ? "rules " : "facts ") + "for literal " + Constant.this.inspect());
				return arityMap.get(arity).iterator();
			} else {		
				return null;
			}
		}
		
		private Iterator<RuleArgument> getNextArgumentIterator() {
			if (ruleIterator.hasNext()) {
				Rule rule = ruleIterator.next();
				if (rule.getConsequent().isUnifiable(Constant.this)) {
					logger.fine(party.toString() + ": found " + (rule.getDob().compareTo(new Double(1.0))==0 ? "" : "defeasible ") + (rule.isFact() ? "fact " : "rule ") + rule.inspect());
					return rule.argumentIterator(Constant.this, this.needed, this.party, this.level, this.d_top, this.valuator, this.restrictedRebutting);
				} else {
					logger.fine(party.toString() + ": ignored " + (rule.getDob().compareTo(new Double(1.0))==0 ? "" : "defeasible ") + (rule.isFact() ? "fact " : "rule ") + rule.inspect());
					return getNextArgumentIterator();
				}
			} else {
				return null;
			}
		}
	}

	@Override
	List<Predicate> getPredicates() {
		ArrayList<Predicate> result = new ArrayList<Predicate>();
		result.add(new Predicate(functor, 0));
		return result;
	}
}
