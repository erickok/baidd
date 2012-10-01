package nl.uu.cs.arg.platform;

import java.util.List;

import nl.uu.cs.arg.shared.dialogue.DialogueMessage;
import nl.uu.cs.arg.shared.dialogue.Move;
import nl.uu.cs.arg.shared.dialogue.locutions.Locution;

/**
 * A simple {@link PlatformListener} implementation that prints all received
 * messages to the command line. 
 * 
 * @author erickok
 */
public class PlatformOutputPrinter implements PlatformListener {

	/**
	 * Singleton that is used by during command line startup of a launcher to print moves, messages and exceptions from the platform.
	 */
	public static PlatformOutputPrinter defaultPlatformOutputPrinter = new PlatformOutputPrinter();

	private PlatformOutputLevel outputLevel;

	/**
	 * Constructor is private: access this printer via its singleton instance defaultPlatformOutputPrinter
	 */
	private PlatformOutputPrinter() {
		this.outputLevel = PlatformOutputLevel.Moves;
	}
	
	/**
	 * Set how verbose this platform listener should print messages to the console
	 */
	public void setLevel(PlatformOutputLevel value) {
		this.outputLevel = value;
	}

	@Override
	public void onExceptionThrown(PlatformException e) {
		// Exceptions are always printed
		System.out.println(e.toString());
	}

	@Override
	public void onMessagesReceived(List<DialogueMessage> messages) {
		if (outputLevel == PlatformOutputLevel.Messages || 
				outputLevel == PlatformOutputLevel.Moves) {
			System.out.println(messages.toString());
		}
	}

	@Override
	public void onMoves(List<Move<? extends Locution>> moves) {
		if (outputLevel == PlatformOutputLevel.Moves) {
			for (Move<? extends Locution> move : moves) {
				System.out.println(move.toString());
			}
		}
	}
	
}
