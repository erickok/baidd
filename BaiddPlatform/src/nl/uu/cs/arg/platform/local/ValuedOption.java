package nl.uu.cs.arg.platform.local;

import org.aspic.inference.Constant;

public class ValuedOption {

	public enum Strategy {
		Unknown,
		Build,
		Destroy,
		Indifferent;
	}
	
	/**
	 * The option to consider, which might come from the knowledge or from an existing proposal
	 */
	private Constant option;
	
	/**
	 * The utility assigned to the option
	 */
	private Integer utility;
	
	/**
	 * The strategy assigned to this option
	 */
	private Strategy strategy;

	public ValuedOption(Constant option) {
		this(option, null, Strategy.Unknown);
	}

	public ValuedOption(Constant option, int utility) {
		this(option, utility, Strategy.Unknown);
	}

	public ValuedOption(Constant option, Strategy strategy) {
		this(option, null, strategy);
	}

	public ValuedOption(Constant option, Integer utility, Strategy strategy) {
		this.option = option;
		this.utility = utility;
		this.strategy = strategy;
	}
	
	/**
	 * Returns the option that was valued
	 * @return The term or constant representing the option 
	 */
	public Constant getOption() {
		return this.option;
	}
	
	/**
	 * The utility assigned to the option
	 * @return An integer representing the utility value, or null if not set
	 */
	public Integer getUtility() {
		return this.utility;
	}
	
	/**
	 * The strategy assigned to this option
	 * @return The strategy enumeration value
	 */
	public Strategy getStrategy() {
		return this.strategy;
	}

	/**
	 * Assign a new utility to this option
	 * @param utility The new utility integer value to assign, or null to unset it
	 */
	public void updateUtility(Integer utility) {
		this.utility = utility;
	}
	
	/**
	 * Assign a new strategy to this option
	 * @param strategy The new strategy to assign, or null to unset it
	 */
	public void updateStrategy(Strategy strategy) {
		this.strategy = strategy;
	}
	
	/**
	 * Returns the option contents as a string, just a the Term or Constant inspect functions
	 * @return A formatted and human-readable string representing this option's contents
	 */
	public String inspect() {
		return option.inspect();
	}
	
	public String toString() {
		return "O[" + inspect() + "]" + (this.utility == null? "": "[" + this.utility + "]") + (this.strategy == Strategy.Unknown? "": "[" + this.strategy.toString() + "]");
	}
	
}
