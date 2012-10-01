package nl.uu.cs.arg.shared.dialogue;

import org.aspic.inference.Constant;
import org.aspic.inference.Term;

/**
 * A goal represents an agents desire to achieve something. This
 * is represented by a {@link Term} or {@link Constant}, which is 
 * the desired future state that causes the goal to be achieved.
 * 
 * @author Eric
 *
 */
public class Goal {

	/**
	 * The term or constant describing this goal
	 */
	private Constant goal;
	
	/**
	 * Instantiate a specific agent goal
	 * @param goal The term or constant describing the goal
	 */
	public Goal(Constant goal) {
		this.goal = goal;
	}
	
	/**
	 * Returns the actual contents of this goal
	 * @return A term or constant describing the goal
	 */
	public Constant getGoalContent() {
		return this.goal;
	}
	
	/**
	 * Returns the goal contents as a string, just a the Term or Constant inspect functions
	 * @return A formatted and human-readable string representing this goal's contents
	 */
	public String inspect() {
		return goal.inspect() + ".";
	}
	
	public String toString() {
		return "G[" + goal.inspect() + "]";
	}

}
