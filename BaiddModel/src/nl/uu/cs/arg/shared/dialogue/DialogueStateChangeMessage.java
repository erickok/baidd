package nl.uu.cs.arg.shared.dialogue;

/**
 * A dialogue message that tells something about a change in the dialogue
 * state. The new state is supplied. If no custom message is given, it just
 * states that what the new state is.
 *  
 * @author erickok
 *
 */
public class DialogueStateChangeMessage extends DialogueMessage {

	/**
	 * The new state of the dialogue
	 */
	private DialogueState newState;
	
	public DialogueStateChangeMessage(DialogueState newState, String message) {
		super(message);
		this.newState = newState;
	}

	public DialogueStateChangeMessage(DialogueState newState) {
		this(newState, "The dialogue state was changed to " + newState.toString());
	}

	/**
	 * Return the new state of the dialogue after the state change
	 * @return A dialogue state
	 */
	public DialogueState getNewState() {
		return this.newState;
	}
	
}
