package nl.uu.cs.arg.shared;

import nl.uu.cs.arg.shared.dialogue.Dialogue;

/**
 * A Participant is an agent that is active in a {@link Dialogue}. It
 * contains no actual running behaviour, but is a reference to which 
 * agents are commenced in the dialogue. It is used for example to see
 * which agent played a move.
 * 
 * @author erickok
 *
 */
public class Participant {

	/**
	 * The unique identifier of a participant within a single session.
	 */
	int id;
	
	/**
	 * The publicly visible, human readable name of the participant. 
	 */
	String name;
	
	public Participant(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
	/**
	 * Returns the unique identifier of a participant within a single session.
	 * @return The unique participant ID
	 */
	public int getID() {
		return this.id;
	}
	
	/**
	 * Returns the publicly visible, human readable name of the participant.
	 * @return The participant name
	 */
	public String getName() {
		return this.name;
	}
	
	@Override
	public int hashCode() {
		return id;
	};
	
	@Override
	public String toString() {
		return "P" + this.getID();
	}
	
}
