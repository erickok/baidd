package org.aspic.inference;

import java.io.Serializable;
import java.util.*;

import org.aspic.inference.writers.ReasonerWriter;

/**
 * <p>Captures the substitutions made in developing a particular
 * Argument.  A Substitution is modelled as a map whose keys 
 * are variables and whose values are the Element that's 
 * substituted for that variable.</p>
 * 
 * <p>If you attempt to add a different element for an existing
 * variable then that variable is removed from the substitution 
 * map and the isConsistant flag returns false.</p>
 *
 * @author mjs (matthew.south @ cancer.org.uk)
 */
public class Substitution implements Cloneable, Serializable {

	private static final long serialVersionUID = 1L;
	// Note that if the internal implementation of substitutionMap or bannedVariablesList changes then the .clone() method will need to be changed appropriately
	// a Map that links variables to their substituted Elements
	private Map<Variable, Element> substitutionMap = new HashMap<Variable, Element>();
	// a List of variables that cannot be used in this substitution, because a contradiction was found deeper in the argument tree.
	private List<Variable> bannedVariablesList = new ArrayList<Variable>();
	
	/**
	 * Default constructor.
	 */
	public Substitution() {} // explicitly defined because of the presence of the private constructor

	// used in .clone()
	private Substitution(Map<Variable, Element> substitutionList, List<Variable> bannedVariablesList) {
		this.substitutionMap = substitutionList;
		this.bannedVariablesList = bannedVariablesList;
	}

	/**
	 * Return a new substitution which has all the elements of this one
	 * (having had the <code>second</code> substitution applied) merged with the <code>second</code>
	 * substitution.
	 *
	 * @param second The substitution to be applied to the elements in this substitution and merged with this one.
	 * @return new, potentially deeper substitution.
	 */		
	public Substitution compose(Substitution second) {
		Substitution newsubs = new Substitution();
		Iterator<Variable> e = this.variables().iterator();
		while (e.hasNext()) {
			Variable key = e.next();
			newsubs.add(key, ((Element) this.get(key)).apply(second));
		}
		newsubs.add(second);
		return newsubs;	
	}

	/**
	 * Adds a new substitution into the substitution list, unless the 
	 * new substitution contradicts an existing substitution, in which 
	 * case the contradicted substitution is removed from the list and 
	 * the isConsistant flag is set to false (irrevocably).
	 * @param key the variable that is to be substituted
	 * @param value the Element that is to be substituted for the variable
	 */
	public void add(Variable key, Element value) {
		if (!bannedVariablesList.contains(key)) {
			if (substitutionMap.containsKey(key)) {
				if (!value.equals(substitutionMap.get(key))) {
					substitutionMap.remove(key);
					bannedVariablesList.add(key);
				}
			} else {
				substitutionMap.put(key, value);
			}
		}
	}
	
	/**
	 * Merge the passed substitution into this one. 
	 * If the passed subsitution is inconsistant then this substitution 
	 * will also be inconsistant.
	 * 
	 * @param substitution The substitution to be added to this one.
	 */
	public void add(Substitution substitution) {
		// add any new banned variables to the banned list
		bannedVariablesList.addAll(substitution.bannedVariablesList);
		// add substitutions from the passed substitution
		Iterator<Variable> variableIterator = substitution.variables().iterator();
		while(variableIterator.hasNext()) {
			Variable variableUniqueName = variableIterator.next();
			this.add(variableUniqueName, substitution.get(variableUniqueName));
		}
	}
	
	/**
	 * Check whether this is a clean substitution or whether
	 * variable inconsistancies were found along the way.
	 * 
	 * @return true if there are no banned variables.
	 */
	public boolean isConsistant() {
		return bannedVariablesList.size()==0;
	}
	
	/**
	 * show view of substitution, e.g.
	 * { X = father(terry, valerie), Y = dog(frodo) }
	 * @return string representation of substitution list
	 */
	public String inspect() {
		Iterator<Variable> itr = this.variables().iterator();
		String result = "{";
		while (itr.hasNext()) {
			Variable key = itr.next();
			result += key + "=" + this.get(key).inspect() + (itr.hasNext() ? ", " : ""); 
		}
		return result += "}";
	}
	
	/**
	 * Allows multiple serialisations of this class.
	 * @param writer a particular serialiser
	 */
	public void write(ReasonerWriter writer) {
		writer.write(this);
	}
	
	/**
	 * Size of substitution.
	 * @return Number of substitutions.
	 */
	public int size() {
		//TODO?: replace all references to this method with .variables.size()
		return substitutionMap.size();
	}
	
	/**
	 * Returns true if this Substitution contains a valid
	 * substitution for the passed variable.
	 * @param key whose prescence in this substitution is to be tested
	 * @return true if this Substitution contains a mapping for this variable
	 */
	public boolean containsVariable(Variable key) {
		return substitutionMap.containsKey(key);
	}
	
	/**
	 * Returns the a list of valid variables in this substitution.
	 * @return a set of valid variables contained within this substitution
	 */
	public Set<Variable> variables() {
		return substitutionMap.keySet();
	}
	
	/**
	 * Returns true if this substitution contains a variable mapping 
	 * for the passed element.
	 * @param value element to be tested for inclusion
	 * @return true if the passed value is included in this substitution
	 */
	public boolean containsElement(Element value) {
		return substitutionMap.containsValue(value);
	}
	
	/**
	 * Gets the element that is mapped by the passed variable. 
	 * @param key the variable to be substituted
	 * @return the element that is used to replace the passed variable
	 */
	public Element get(Variable key) {
		return substitutionMap.get(key);
	}
	
	public Object clone() {
		return new Substitution((Map<Variable, Element>) ((HashMap<Variable, Element>) this.substitutionMap).clone(), (List<Variable>) ((ArrayList<Variable>) this.bannedVariablesList).clone());
	}

	/*
	 * Returns true if the passed substitution contains the same
	 * valid variable mappings and both substitutions have the
	 * same consistancy.
	 */
	public boolean equals(Object substitution) {
		return (substitution instanceof Substitution) &&
				this.isConsistant()==((Substitution) substitution).isConsistant() && 
				this.substitutionMap.equals(((Substitution) substitution).substitutionMap);
	}
}
