package org.aspic.inference;

import java.io.Serializable;
import java.util.*;

import org.aspic.inference.writers.ReasonerWriter;


/**
 * A List of Arguments and a Substitution that is common to those Arguments.
 * Used to represent sub-arguments in an argument tree.
 * 
 * @author mjs (matthew.south@cancer.org.uk)
 *
 */
public class RuleArgumentList implements Cloneable, Serializable {
	private Substitution substitution;
	private List<RuleArgument> arguments;

	/**
	 * Default constructor.
	 */
	public RuleArgumentList() {
		arguments = new ArrayList<RuleArgument>();
		substitution = new Substitution();
	}
	
	/**
	 * Typical constructor.
	 * @param arguments list of arguments
	 * @param substitution substitution used in the list of arguments
	 */
	public RuleArgumentList(List<RuleArgument> arguments, Substitution substitution) {
		this.arguments = arguments;
		this.substitution = substitution;
	}

	/**
	 * Getter for argument list.
	 * @return arguments in this list
	 */
	public List<RuleArgument> getArguments() {
		return arguments;
	}
	/*
	public void setArguments(List<Argument> arguments) {
		this.arguments = arguments;
	}
	*/
	/**
	 * Getter for substitution.
	 * @return this argument lists's substitution
	 */
	public Substitution getSubstitution() {
		return substitution;
	}

	/*
	public void setSubstitution(Substitution substitution) {
		this.substitution = substitution;
	}
	*/
	/**
	 * Return cloned ArgumentList that includes the presented argument and it's substitutions.
	 * @param argument argument to add to list.
	 * @return cloned and extended RuleArgumentList
	 */
	RuleArgumentList cloneAndExtend(RuleArgument argument) {
		List<RuleArgument> newarguments = new ArrayList<RuleArgument>();
		// clone current list of arguments
		Iterator<RuleArgument> itr = arguments.iterator();
		while (itr.hasNext()) {
			newarguments.add(itr.next());
		}
		// add new argument, and return a new ArgumentList that incorporates the new argument and substitution
		newarguments.add(argument);
		return new RuleArgumentList(newarguments, substitution.compose(argument.getSubstitution()));
	}
	
	/**
	 * 
	 * @param valuator
	 * @return
	 */
	Double valuate(RuleArgumentValuator valuator) {
		return valuator.valuate(this);
	}
	/**
	 * Check to see if any argument in this list is semantically 
	 * equal (see <code>Argument.isSemanticallyEqual</code>) to the 
	 * presented candidate Argument.
	 * 
	 * @param candidate candidate Argument to checked against this list.
	 * @return true iff candidate is semantically equal to any argument in the list
	 */
	public boolean includesSemanticallyEqual(RuleArgument candidate) {
		Iterator<RuleArgument> argumentIterator = arguments.iterator();
		while (argumentIterator.hasNext()) {
			if (candidate.isSemanticallyEqual(argumentIterator.next())==true) return true;
		}
		return false;
	}
	
	/**
	 * Show Names of enclosed Arguments
	 * @return Names of enclosed Arguments
	 */
	public String inspect() {
		StringBuffer sb = new StringBuffer("[");
		Iterator<RuleArgument> argIterator = arguments.iterator();
		while (argIterator.hasNext()) {
			sb.append(argIterator.next().getName());
			if (argIterator.hasNext()) sb.append(", ");
		}
		sb.append("]");
		return sb.toString();
	}
	
	public void write(ReasonerWriter writer) {
		writer.write(this);
	}
	
	/**
	 * Get a ConstantList of each of the argument's claims.
	 * @return list of main Argument claims.
	 */
	public ConstantList getClaims() {
		ConstantList list = new ConstantList();
		Iterator<RuleArgument> argIterator= arguments.iterator();
		while (argIterator.hasNext()) {
			list.add(argIterator.next().getClaim());
		}
		return list;
	}

	/**
	 * Check to see if any argument in this list can be unified with the passed argument.
	 * @param candidate
	 * @return true if the candidate can be unified with a member of this list
	 */
	public boolean includesUnifiable(RuleArgument candidate) {
		for (RuleArgument argument : arguments) {
			if (candidate.isUnifiable(argument)) return true;
		}
		return false;
	}
	/*
	public Argument get(int index) {
		return arguments.get(index);
	}
	*/
	/*
	public boolean add(Argument arg) {
		return arguments.add(arg);
	}
	
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e); // Not expecting this error.
		}
	}
	*/
}
