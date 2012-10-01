package nl.uu.cs.arg.shared.dialogue.protocol;

import nl.uu.cs.arg.shared.dialogue.DialogueMessage;

/**
 * A simple message object that contains the cause of a dialogue 
 * termination.
 * 
 * @author Eric
 *
 */
public class TerminationMessage extends DialogueMessage {

	public TerminationMessage(String cause) {
		super(cause);
	}

	@Override
	public String toString() {
		return "Dialogue will terminate: " + this.getMessage();
	}
	
}
