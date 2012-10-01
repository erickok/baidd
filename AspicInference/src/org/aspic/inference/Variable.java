package org.aspic.inference;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import org.aspic.inference.writers.KnowledgeWriter;


/**
 * <p>A Variable is an ungrounded placeholder, used within a rule
 * or within a query predicate expression that is grounded during the 
 * process of argument discovery.</p>
 * 
 * <p>This Element has no argumentIterator method as it makes no sense
 * to generate arguments with a variable as it's claim. 
 * In swi-prolog the goal "?- X." yields the message:<pre> 
 * ... 1,000,000 ............ 10,000,000 years later 
 *      >> 42 << (last release gives the question)</pre></p>
 *      
 * <p>A variable has a public name and an internal name. The public
 * name is the one that it shows to the outside world.  The 
 * unique name is is used in argument development, to avoid
 * certain ambiguous cases, namely:
 * <h4>Case 1:</h4>
 * likes(sam, X) should unify with likes(X, april) to produce 
 * likes(sam, april) and the substitution {X_0=sam,X_1=april}.
 * <h4>Case 2:</h4>
 * likes(X, X) should not unify with likes(sam, april).</p>
 * 
 * <p>The two X's in Case 1 are in separate expressions, so the fact 
 * that they share the same public name is a coincidence and should 
 * not affect the unification.  This difference is reflected in 
 * their different internalNames - as can be seen in the substitution.</p>
 * 
 * <p>The two X's in case 2 are in the same expression so the fact that
 * they share the same public name means that they can only be substituted
 * with the same fact, thus the unification fails.</p>
 *
 * @author mjs (matthew.south @ cancer.org.uk)
 *
 */
public class Variable extends Element {
	
	// private static int counter = 0;
	// used to assign internal names. The key is the public name and the value is the next counter.
	private static HashMap<String, Integer> names = new HashMap<String, Integer>();
	
	private String name;
	private String internalName=null;
	
	/** 
	 * Typical Constructor.  Automatically generates the internal name. 
	 * @param name public name of variable. 
	 **/
	public Variable(String name) {
		this.name = name;
		this.internalName = generateUniqueName();
	}
    
	/**
	 * Default constructor.
	 */
    public Variable(){}

    /**
     * Getter for internal name.
     * @return internal name.
     */
	public String getInternalName() {
		return internalName;
	}
	
	void setInternalName(String uniqueName) {
		this.internalName = uniqueName;
	}
	
	/**
	 * Getter for public name.
	 * @return public name of variable.
	 */
	public String getName() {
		return name;
	}
    
	/**
	 * Setter for name.  Generates new internal name.
	 * @param name public name.
	 */
    public void setName(String name) {
        this.name = name;
		this.internalName = generateUniqueName();
   }    
    /*
    public String toStringXml(){
        return  "<variable>" + 
                "<name>" + getName() + "</name>" + 
                "<internalName>" + getInternalName() + "</internalName>" +
                "</variable>";
    }    
	*/
	public String inspect() {
		return name;
	}
	
	/**
	 * Return the substitution listed for this variable name (if there is one),
	 * else this variable 
	 */
	public Element apply(Substitution subs) {
		if (subs.containsVariable(this)) {
			return (Element) subs.get(this);
		} else {
			return this;
		}
	}
	
	/**
	 * A Variable can be unified any Element. 
	 */
	public boolean isUnifiable(Element toUnify) {
		return true;
	}
	
	/**
	 * Return substitution extended with this variable + Element.
	 */
	public Substitution unify(Element toUnify, Substitution subs) {
		subs.add(this, toUnify);
		return subs;
	}

	/**
	 * return true if testClause is a variable, else false
	 */
	boolean isEqualModuloVariables(Element testClause) {
		return (testClause instanceof Variable);
	}
	
	boolean isGrounded() {
		return false;
	}

	public void write(KnowledgeWriter writer) {
		writer.write(this);
	}
	
	List<Variable> getVariables() {
		List<Variable> list = new ArrayList<Variable>();
		list.add(this);
		return list;
	}
	/*
	public String argumentTag(Double needed) {
		// TODO: Is this Needed? - if not it should be moved from Element to Constant?
		return name + "_" + needed.toString();
	}
	*/
	
	public Object clone() throws CloneNotSupportedException {
		return (Variable) super.clone();
	}
	
	private String generateUniqueName() {
		// slightly awkward because you can't increment an immutable Integer object with ++
		Integer counter = names.get(name);
		if (counter==null) {
			counter=0;
			names.put(name, counter);
		}
		String result = name + "_" + counter;
		names.put(name, counter+1); // put the next name in.
		return result;
	}
	
	
	public int hashCode() {
		return internalName.hashCode();
	}
	
	public boolean equals(Object test) {
		return (test instanceof Variable) &&
				((Variable) test).getInternalName().equals(this.getInternalName());
	}

	@Override
	List<Predicate> getPredicates() {
		return new ArrayList<Predicate>();
	}
	
	/*
	public int hashCode() {
		return 0;
	}
	
	public boolean equals(Object test) {
		return (test instanceof Element) && this.isEqualModuloVariables((Element) test);
	}
	*/
}
