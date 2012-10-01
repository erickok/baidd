package nl.uu.cs.arg.shared.dialogue.protocol;

import nl.uu.cs.arg.shared.dialogue.DialogueException;
import nl.uu.cs.arg.shared.dialogue.Move;
import nl.uu.cs.arg.shared.dialogue.locutions.Locution;

/**
 * An exception that occurs when a protocol rule has been violated.
 * The reason should be included as a message.
 * 
 * @author erickok
 *
 */
public class ProtocolException extends DialogueException {

	private static final long serialVersionUID = 1L;
	
	/**
	 * The move that caused the protocol violation, creating this exception 
	 */
	private Move<? extends Locution> cause;

	public ProtocolException(Move<? extends Locution> cause, String violationDescription) {
		super(violationDescription);
		this.cause = cause;
	}

	/**
	 * Returns the move that caused the protocol violation, creating this exception
	 * @return The move that is the cause to this exception
	 */
	public Move<? extends Locution> getCauseMove() {
		return this.cause;
	}
	
	@Override
	public String toString() {
		return this.cause.toLogicString() + " caused a protocol exception: " + super.toString();
	}
	
}
