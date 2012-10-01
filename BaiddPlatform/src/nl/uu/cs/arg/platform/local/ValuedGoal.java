package nl.uu.cs.arg.platform.local;

import nl.uu.cs.arg.shared.dialogue.Goal;

import org.aspic.inference.Constant;

/**
 * A valued goal assigns a utility value to an agent's goal.
 * 
 * @author Eric
 *
 **/
public class ValuedGoal extends Goal {

	/**
	 * The utility value assigned to the goal
	 */
	private int utility;
	
	public ValuedGoal(Constant goal, int utility) {
		super(goal);
		this.utility = utility;
	}

	/**
	 * The utility assigned to the goal
	 * @return An integer representing the utility value
	 */
	public int getUtility() {
		return this.utility;
	}

	/**
	 * Returns the goal contents as a string, just a the Term or Constant inspect functions
	 * @return A formatted and human-readable string representing this goal's contents
	 */
	public String inspect() {
		return super.inspect() + " " + utility;
	}
	
	public String toString() {
		return super.toString() + "[" + this.utility + "]";
	}
	
}
