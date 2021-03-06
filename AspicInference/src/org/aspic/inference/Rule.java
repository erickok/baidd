package org.aspic.inference;

import org.aspic.inference.writers.*;

import java.util.*;
import java.util.logging.Logger;

/**
 * <p>A Rule is a specialised Term, rule/4. The four parameters are:
 * <ul>
 * <li> parameter 1: <code>consequent</code> (a Constant or Term)</li>
 * <li> parameter 2: <code>antecedent</code> (an ElementList of Constants or Terms) </li>
 * <li> parameter 3: degree of belief, <code>dob</code> (a numeric Constant) </li>
 * <li> parameter 4: rule <code>name</code> (a Constant) </li>
 * </ul> 
 * Rule is also the only class that is explicitly allowed
 * into the Knowledge Base.</p>
 * 
 * <p>The first two parameters arise naturally from a prolog (i.e. horn
 * clause based) rule system.  The last two parameters fix us into the 
 * argumentation space and need some explanation.</p>
 * <p><code>dob</code> allows us to place an ordering over arguments that 
 * are developed from facts and rules,List<Element> when we develop those arguments
 * using a particular evaluation strategy (i.e. weakest_link or last_link).</p>
 * <p><code>name</code> allows a particular rule to be undercut by other
 * rules and facts.  It is itself a Term and can be thought of as a "hidden premise".
 * because when developing an argument based on a rule, the algorithm checks 
 * that it can develop arguments for each of the rule's premises
 * (members of the antecedent) *and* it's name.</p>  
 * <h4>A knowledge base is a collection of Rules</h4>
 * <p>A KnowledgeBase contains a list of rules.  Facts and beliefs are
 * represented in a KnowledgeBase as Rules with an empty antecedent.
 * In order to distinguish between proper rules (i.e. those
 * that have non-empty antecendents) and facts/beliefs a helper method,
 * isFact() is provided.</p>
 * <h4>Link between rule.name and knowledge base.</h4>
 * <p>Because of the "hidden premise" nature of a rule's name, a knowledge
 * base automatically adds a rule's name as a fact/belief when you add
 * a rule to the knowledge base.  This automatically generated fact/belief
 * is known as the rule hook.  If the rule is proper and unnamed then
 * the knowledge base automatically assigns the rule a name and adds
 * that name to the knowledgebase.  Care is taken that automatically 
 * assigned names do not conflict with user assigned names.  To help
 * with this process, <code>isAutoNamed</code> and <code>isAutoGenerated</code> 
 * properties are provided. 
 * <h4>Caption and Description</h4>
 * <p>Caption and Description fields allow human users to mark-up a rule
 * with a human-readable identifier (caption) and description.</p>
 * @author mjs (matthew.south @ cancer.org.uk)
 */
public class Rule extends Term {
	private static final long serialVersionUID = 1L; // default id
// TODO: Use inheritance to create RuleWithCaption that overides this class which should be faster if it doesnt have to worry about captions in the apply() method.
/* TODO?: Move Rule to inherit from Element, not Term because
 * .negation doesnt mean anything for this class which suggests problem in inheritance tree
 * Also it shouldnt be possible to embed a rule in a Term.
 * The reason not to do this is because you lose some nice inherited behaviour, 
 * i.e. isUnifiable, unify, isEqualModuloVariables, isGrounded, ruleTag, setKnowledgeBase
 * Update. I tried this and it wasnt pretty.
 * Unification stopped working.  I think because inheriting from Term means the instanceof condition holds in the Term.unify() implementation.
 */
	private static Logger logger = Logger.getLogger(Rule.class.getName());		

	private boolean isAutoGenerated = false; // if true this rule has been autogenerated
	private boolean isAutoNamed = false; // if true, this rule's name has been autogenerated.

    private String caption;
    private String description;
    private String claimCaption;
    private String claimDescription;
    
	/**
	 * Default constructor.
	 */
	public Rule() {
		super("rule", new Constant(), new ElementList(), new ConstantFloat(1.0));
	}

	/** 
	 * Constructor for a fact
	 * @param fact strict constant/term
	 */
	public Rule(Constant fact) {
		super("rule", fact, new ElementList(), new ConstantFloat(1.0));
	}

	/** 
	 * Constructor for a fact
	 * @param fact strict constant/term
	 * @param name consumer supplied rule name
	 */
	public Rule(Constant fact, Constant name) {
		super("rule", fact, new ElementList(), new ConstantFloat(1.0), name);
	}

	/** 
	 * Constructor for a belief (a defeasible assertion, with belief less than 1.0)
	 * @param belief defeasible constant/term
	 * @param dob degree of belief (0, 1.0]
	 */
	public Rule(Constant belief, Double dob) {
		super("rule", belief, new ElementList(), new ConstantFloat(dob));
		checkDob(dob);
	}

	/** 
	 * Constructor for a belief (a defeasible assertion, with belief less than 1.0)
	 * @param belief defeasible constant/term
	 * @param dob degree of belief (0, 1.0]
	 * @param name consumer supplied rule name
	 */
	public Rule(Constant belief, Double dob, Constant name) {
		super("rule", belief, new ElementList(), new ConstantFloat(dob), name);
		checkDob(dob);
	}

	/**
	 * Constructor for a strict rule
	 * @param consequent claim
	 * @param antecedent premises
	 */
	public Rule(Constant consequent, ElementList antecedent) {
		super("rule", consequent, antecedent, new ConstantFloat(1.0));
	}

	/**
	 * Constructor for a defeasible rule
	 * @param consequent claim
	 * @param antecedent premises
	 * @param dob degree of belief (0, 1.0]
	 */
	public Rule(Constant consequent, ElementList antecedent, Double dob) {
		super("rule", consequent, antecedent, new ConstantFloat(dob));
		checkDob(dob);
	}
	
	/**
	 * General purpose constructor
	 * @param consequent claim
	 * @param antecedent premises
	 * @param dob degree of belief (0, 1.0]
	 * @param name consumer supplied rule name
	 */
	public Rule(Constant consequent, ElementList antecedent, Double dob, Constant name) {
		super("rule", consequent, antecedent, new ConstantFloat(dob), name);
		checkDob(dob);
		checkRuleName();
	}
	
	/**
	 * Protected constructor for use in .apply method
	 * protected because there are some constraints on the structure of 
	 * the argList that cannot be externally guaranteed.
	 * @param argList
	 */
	Rule(ElementList argList) {
		// dodgy constructor - contains some big assumptions, 
		// should be internally consistant, 
		// but cannot be externally guaranteed (hence package visibility) 
		super("rule", argList);
	}
    
    public boolean equals(Object obj){
        if (!(obj instanceof Rule)){
            return false;
        }
        Rule copy = (Rule)obj;        
        if ((copy.getCaption() == null) && (this.getCaption() != null)){
            return false;
        }
        if ((copy.getDescription() == null) && (this.getDescription() != null)){
            return false;
        }
        if ((copy.getFunctor() == null) && (this.getFunctor() != null)){
            return false;
        }
     
        if (
                (
                        (copy.getCaption() == null) || 
                        (copy.getCaption().equals(this.getCaption()))
                ) &&            
                (
                        (copy.getDescription() == null) ||
                        (copy.getDescription().equals(this.getDescription())) 
                ) &&
                (
                        (copy.getFunctor() == null) ||
                        (copy.getFunctor().equals(this.getFunctor())) 
                ) &&            
            (copy.isAutoGenerated() == this.isAutoGenerated()) &&
            (copy.isAutoNamed() == this.isAutoNamed()) &&
            (copy.getArgList().equals(this.getArgList())))
            {
                return true;
            }
        else{
            return false;
        }
    }
    /*
    public String toStringXml(){
        StringBuffer result = new StringBuffer("<rule>" + 
                                               "<functor>" + getFunctor() + "</functor>" + 
                                               "<caption>" + getCaption() + "</caption>" +
                                               "<description>" + getDescription() + "</description>"  + 
                                               "<isAutoGenerated>" + isAutoGenerated() + "</isAutoGenerated>" + 
                                               "<isAutoNamed>" + isAutoNamed() + "</isAutoNamed>" + 
                                               getArgList().toStringXml() +
                                               "</rule>");
        return result.toString();
    }
    */
    public int hashCode(){
        return this.toString().hashCode();
    }    
    
	/**
	 * Getter for Rule consequent.
	 * @return The Rule's consequent (or head).
	 */
	public Constant getConsequent() {
		return (Constant) super.getArg(0);
	}

	/**
	 * Setter for the Rule's consequent.
	 * @param consequent A Constants or Terms.
	 */
	public void setConsequent(Constant consequent) {
		consequent.setKnowledgeBase(this.getKnowledgeBase());
		super.setArg(0, consequent);
		checkRuleName();
	}
	
	/**
	 * Getter for Rule's antecendent.
	 * @return The Rule's antecedent (or tail).
	 */ 
	public ElementList getAntecedent() {
		return (ElementList) super.getArg(1);
	}

	/**
	 * Setter for Rule's antecedent.
	 * @param antecedent An ElementList of Constants or Terms.
	 */
	public void setAntecedent(ElementList antecedent) {
		antecedent.setKnowledgeBase(this.getKnowledgeBase());
		super.setArg(1, antecedent);
		checkRuleName();
	}

	/**
	 * Getter for Rule's Degree of Belief.
	 * @return degree of belief for Rule (0, 1.0]
	 */
	public Double getDob() {
		return Double.valueOf(((Constant) super.getArg(2)).getFunctor());
	}

	/**
	 * Setter for Rule's Degree of Belief.
	 * @param dob Degree of Belief (0, 1.0]
	 */
	public void setDob(Double dob) {
		checkDob(dob);
		ConstantFloat constantDob = new ConstantFloat(dob);
		constantDob.setKnowledgeBase(this.getKnowledgeBase());
		super.setArg(2, constantDob);
	}
	
	/**
	 * Getter for Rule name.
	 * @return the Rule's name, or null if it's not yet been set.
	 */
	public Constant getName() {
		if (super.numberOfArgs()<4) {
			return null;
		} else {
			return (Constant) super.getArg(3);
		}
	}
	
	/**
	 * Setter for Rule name.
	 * @param name the rule's name to set. 
	 */
	public void setName(Constant name) {
		if (super.numberOfArgs()==3) {
			super.addArg(3, name); // if name was previously unset
		} else {
			super.setArg(3, name); // if we are changing the name
		}
		checkRuleName();
	}

	public String inspect() {
		return inspect(true, true);
	}
	
	/**
	 * A version of inspect that allows you to optionally hide all of the auto generated artifacts.
	 * @param showAutoGeneratedRuleNames Allows you to hide automatically generated rule names
	 * @param showRuleHooks Allows you to hide the automatically generated "rule hook" rules 
	 * @return String representing a Prolog representation of the rule.
	 */
	public String inspect(boolean showAutoGeneratedRuleNames, boolean showRuleHooks) {
		if (!this.isAutoGenerated || showRuleHooks) {
			
			String claimCaption = (getClaimCaption()==null || getClaimCaption().equalsIgnoreCase("null"))? "" : " $"+getClaimCaption()+"$ ";
			String caption  = 	(getCaption()==null || getCaption().equalsIgnoreCase("null"))?  "": " $"+getCaption()+"$ " ;
			caption = caption + claimCaption;
			
			return (getName()!=null && (!this.isAutoNamed || showAutoGeneratedRuleNames) ? "[" + getName().inspect() + "] " : "") + getConsequent().inspect() +
					((getAntecedent()!=null && getAntecedent().size()>0) ? " <- " + getAntecedent().inspect() : "") +
					(getDob().compareTo(new Double(1.0))==0 ? "" : " " + getDob().toString());
			
			
		} else {
			return "";
		}
	}

	
	public Rule apply(Substitution subs) {
		if (this.isGrounded()) {
			return this;
		} else {
			// Next line a little awkward - refactor?
			Rule rule = new Rule(super.applyArgs(subs));
			// set the caption of the new Rule with placeholders instantiated if possible
			if (this.getClaimCaption()!=null) {
				String[] toBeInstantiated = this.getClaimCaption().split("\\{");
				StringBuffer buffer = new StringBuffer();
				buffer.append(toBeInstantiated[0]);
				if (toBeInstantiated.length>0) {
					// work through array, missing first element, replacing substitutions where possible
					for (int i=1; i<toBeInstantiated.length; i++) {
						if (toBeInstantiated[i].indexOf("}")>0) {
							String variable = toBeInstantiated[i].split("\\}")[0];
							boolean isReplaced=false;	
							for (Variable match : subs.variables()) {
								if (match.getName().equals(variable) && !isReplaced) {
									buffer.append("{");
									buffer.append(subs.get(match).inspect());
									buffer.append("}");
									buffer.append(toBeInstantiated[i].substring(variable.length()+1));
									isReplaced=true;
								}
							}
							if (!isReplaced) buffer.append("{" + toBeInstantiated[i]);
						}
					}
				}
				rule.setClaimCaption(buffer.toString());
			}
			if (this.getCaption()!=null) {
				String[] toBeInstantiated = this.getCaption().split("\\{");
				StringBuffer buffer = new StringBuffer();
				buffer.append(toBeInstantiated[0]);
				if (toBeInstantiated.length>0) {
					// work through array, missing first element, replacing substitutions where possible
					for (int i=1; i<toBeInstantiated.length; i++) {
						if (toBeInstantiated[i].indexOf("}")>0) {
							String variable = toBeInstantiated[i].split("\\}")[0];
							boolean isReplaced=false;	
							for (Variable match : subs.variables()) {
								if (match.getName().equals(variable)) {
									buffer.append("{");
									buffer.append(subs.get(match).inspect());
									buffer.append("}");
									buffer.append(toBeInstantiated[i].substring(variable.length()+1));
									isReplaced=true;
								}
							}
							if (!isReplaced) buffer.append(toBeInstantiated[i]);
						}
					}
				}
				rule.setCaption(buffer.toString());
			}			
			rule.setKnowledgeBase(this.getKnowledgeBase());
			return rule;			
		}
	}
	
	/**
	 * Interrogation method to see if this rule actually
	 * is a rule, or a rule without a body, which is a fact.
	 * @return true if this rule is actually a fact
	 */
	public boolean isFact() {
		return (this.getAntecedent() == null  || this.getAntecedent().size()==0);
	}
	
	/**
	 * Check whether Rule is defeasible or strict.
	 * @return true if this object is a rule and it's dob is 1.0
	 */
	public boolean isStrict() {
		return (this.getDob().compareTo(new Double(1.0))==0);
	}

	public void write(KnowledgeWriter writer) {
		writer.write(this);
	}
	
	/**
	 * Checks whether this rule was automatically named.
	 * @return true if rule is not face and rule name was automatically allocated.
	 */
	boolean isAutoNamed() {
		return this.isAutoNamed;
	}
	
	/**
	 * Checks whether this rule was automatically generated.  i.e. is a rule hook.
	 * @return true if rule is an automatically generated rule hook.
	 */
	public boolean isAutoGenerated() {
		return this.isAutoGenerated;
	}
	
	public Object clone() {
		Object o = null;
		try {
			o = super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return o;
	}
	
	/**
	 * Set AutoGenerated flag.
	 * @param isAutoGenerated true if this rule has been automatically generated (i.e. it is a rule hook or a transposed rule).
	 */
	void setAutoGenerated(boolean isAutoGenerated) {
		this.isAutoGenerated = isAutoGenerated;
	}

	/**
	 * Set AutoNamed flag.
	 * @param isAutoNamed true if this proper rule has been automatically named
	 */
	void setAutoNamed(boolean isAutoNamed) {
		this.isAutoNamed = isAutoNamed;
	}
	
	private void checkDob(Double dob) {
		if (dob > 1.0 || dob <= 0.0) {
			throw new RuntimeException ("Degree of Belief, " + dob.toString() + " is outside of the boudns (0,1.0]");
		}
	}
	
	private void checkRuleName() {
		List<Variable> variables = getConsequent().getVariables();
		if (getAntecedent()!=null) {
			variables.addAll(getAntecedent().getVariables());
		}
		if (getName()!=null && (variables.size()>0) && getName().getVariables().size()>0) {
			if (!variables.containsAll(getName().getVariables())) {
				throw new RuntimeException ("Rule name cannot contain variables that are not in the rule.");
			}
		}
	}

	/**
	 * Return an iterator for all arguments that can be developed from this
	 * rule, assuming the passed literal can be unified with the rule's
	 * consequent.
	 * 
	 * @param literal possible instantiation of rule's consequent
	 * @param needed a threshold of support needed by the sought arguments
	 * @param party the party who's developing the arguments (useful for logging)
	 * @param level level the current level of recursion
	 * @param d_top d_top the distance from the top
	 * @return Iterator over all arguments for literal, based on this rule.
	 */
	Iterator<RuleArgument> argumentIterator(Constant literal, Double needed, Party party, int level, int d_top, RuleArgumentValuator valuator, boolean restrictedRebutting) {
		if (literal.isUnifiable(this.getConsequent())) {
			// get mgu (most general unifier) and apply it
			Substitution unifier = literal.unify(this.getConsequent(), new Substitution());
			Rule instantiation = this.apply(unifier);
			if (unifier.size()>0) logger.fine(party.toString() + ": instantiated to " + instantiation.inspect());
			return new RuleArgumentIterator(instantiation, unifier, needed, party, level, d_top, valuator, restrictedRebutting);
		} else {
			logger.fine(party.toString() + ": unification " + literal.inspect() + " ~> " + getConsequent().inspect() + " fails.");
			return null;
		}
	}

	List<Predicate> getPredicates() {
		List<Predicate> result = getArgList().getPredicates();
		if (this.isAutoNamed) {
			/* Next line bakes in the assumption that an automatically generated rule name consists of a single constant, not a term */
			Predicate toRemove = new Predicate(this.getName().getFunctor(), 0);
			if (result.contains(toRemove)) {
				result.remove(toRemove);
			} else {
				System.err.print("failed to remove predicate : " + toRemove.toString());
			}
		}
		return result;
	}

	/**
	 * <p>Used by Rule.argumentIterator(...).
	 * Develops all arguments based on this rule.  Like the other
	 * ArgumentIterators (for Constant and ElementList), the difficult
	 * thing in this is knowing whether there is another Argument that
	 * can be generated, so hasNext() looks for that Argument, and if
	 * it can find it, queues it up.</p>
	 * <p>Assumes user will poll hasNext() before calling next() after the first argument has been returned.</p>  
	 * 
	 * @author mjs (matthew.south @ cancer.org.uk)
	 */
	private class RuleArgumentIterator implements Iterator<RuleArgument> {
		/* When generating an Argument from a rule, some or all of 
		 * the free variables may be grounded by the passed literal.
		 * The instantiation is the (more) grounded rule, and the 
		 * unifier is the unification used to ground it.*/
		private Rule instantiation;
		private Substitution unifier;
		// these are standard parameters to all argument iterators.
		private Double needed;
		private Party party;
		private int level;
		private int d_top;
		private RuleArgumentValuator valuator;
		private boolean restrictedRebutting;
		
		private RuleArgument nextArgument=null;
		private boolean queuedArgument=false;
		
		// these are used for developing sub-arguments
		private ElementList coisa;
		private Iterator<RuleArgumentList> argumentListIterator;
		
		public RuleArgumentIterator(Rule instantiation, Substitution unifier, Double needed, Party party, int level, int d_top, RuleArgumentValuator valuator, boolean restrictedRebutting) {
			this.instantiation = instantiation;
			this.unifier = unifier;
			this.needed = needed;
			this.party = party;
			this.level = level;
			this.d_top = d_top;
			this.valuator = valuator;
			this.restrictedRebutting = restrictedRebutting;
			// coisa - conclusions of immediate sub-arguments.
			coisa = (ElementList) instantiation.getAntecedent().copy();
			// clever bit - if a rule is a proper rule, then it's name is added as a "hidden" premise.
			if (!instantiation.isFact()) coisa.add(instantiation.getName()); // add the name as the last premise so that it picks up the substitutions made in earlier argument generation.
			if (coisa.size()>0) logger.fine(party.toString() + ": searching for arguments for: " + coisa.inspect() + ".");
			argumentListIterator = coisa.argumentIterator(needed, party, level+1, d_top+1, valuator, restrictedRebutting);
			queuedArgument = hasNext();
		}
		
		public boolean hasNext() {
			if (queuedArgument==true) {
				return true;
			} else {
				if (argumentListIterator.hasNext()) {
					RuleArgumentList subArgs = argumentListIterator.next();
					Substitution deeperUnifier = unifier.compose(subArgs.getSubstitution()); 
					// get support from sub-args
					//Double support = (subArgs.getArguments().size()==0) ? Rule.this.getDob() : subArgs.getSupport();
					Double support = (subArgs.getArguments().size()==0) ? Rule.this.getDob() : subArgs.valuate(valuator);
					if (support>=needed) {
						Rule topRule = instantiation.apply(deeperUnifier);
						/*
						topRule.claimCaption = instantiation.claimCaption;
						topRule.caption = instantiation.caption;
						*/
						nextArgument = new RuleArgument(topRule, support, deeperUnifier, subArgs, party, level, d_top, valuator, restrictedRebutting);
						queuedArgument=true;
						return true;
					} else {
						return this.hasNext();
					}
				} else {
					return false;
				}
			}
		}
		
		public RuleArgument next() {
			if (queuedArgument) {
				queuedArgument=false;
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
	 * Getter for caption.
	 * @return human readable identifier for rule.
	 */
    public String getCaption() {
        return caption;
    }

    /**
     * Setter for caption.
     * @param caption human readable identifier for rule.
     */
    public void setCaption(String caption) {
        this.caption = caption;
    }

    /**
     * Getter for description.
     * @return Human readable description of rule.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Setter for description.
     * @param description Human readable description of rule.
     */
    public void setDescription(String description) {
        this.description = description;
    }

	public String getClaimCaption() {
		return claimCaption;
	}

	public void setClaimCaption(String claimCaption) {
		this.claimCaption = claimCaption;
	}

	public String getClaimDescription() {
		return claimDescription;
	}

	public void setClaimDescription(String claimDescription) {
		this.claimDescription = claimDescription;
	}

	private static String replace(String toBeReplaced, String toReplaceWith, String string) {
		int start = string.indexOf(toBeReplaced);
		if (start == -1) {
			return string;
		}
		StringBuffer sb = new StringBuffer();
		sb.append(string.substring(0, start));
		sb.append(toReplaceWith);
		sb.append(string.substring(start+toBeReplaced.length()));
		return sb.toString();
	}
}

