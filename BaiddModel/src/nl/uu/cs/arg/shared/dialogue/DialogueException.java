package nl.uu.cs.arg.shared.dialogue;

/**
 * An exception thrown by the Dialogue, for example when 
 * inconsistencies arise.
 * 
 * @author erickok
 *
 */
public class DialogueException extends Exception {

	private static final long serialVersionUID = 1L;

	public DialogueException(String errorMessage) {
		super(errorMessage);
	}

	@Override
	public String toString() {
		return this.getMessage();
	}
	
}
