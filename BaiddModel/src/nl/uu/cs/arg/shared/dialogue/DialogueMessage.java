package nl.uu.cs.arg.shared.dialogue;

/**
 * A simple container for messages that should be broadcasted around the 
 * platform (e.g. on dialogue status changes).
 * 
 * @author erickok
 *
 */
public class DialogueMessage {

	/**
	 * The message text
	 */
	private String message;
	
	public DialogueMessage(String message) {
		this.message = message;
	}
	
	/**
	 * Returns a message about something that happened in the dialogue
	 * @return A message string
	 */
	public String getMessage() {
		return this.message;
	}

	@Override
	public String toString() {
		return this.message;
	}
}
