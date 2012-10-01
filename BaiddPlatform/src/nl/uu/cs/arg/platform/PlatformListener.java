package nl.uu.cs.arg.platform;

import java.util.List;

import nl.uu.cs.arg.shared.dialogue.DialogueMessage;
import nl.uu.cs.arg.shared.dialogue.Move;
import nl.uu.cs.arg.shared.dialogue.locutions.Locution;

/**
 * An interface for all listeners to a {@link Platform}. A platform, 
 * that runs in its own Thread, will send useful (error) messages 
 * through this interface. A class, like a GUI, can listen to these 
 * and present them to the user in an appropriate way.
 * 
 * @author erickok
 *
 */
public interface PlatformListener {

	/**
	 * When a set of {@link Move}s was submitted by one of the {@link Agent}s in the dialogue
	 * @param move The moves that were submitted to the platform
	 */
	public void onMoves(List<Move<? extends Locution>> moves);
	
	/**
	 * When a message about the ongoing dialogue was received
	 * @param messages The messages that were received
	 */
	public void onMessagesReceived(List<DialogueMessage> messages);
	
	/**
	 * When an exception was thrown by the platform
	 * @param e The exception that was thrown
	 */
	public void onExceptionThrown(PlatformException e);

}
