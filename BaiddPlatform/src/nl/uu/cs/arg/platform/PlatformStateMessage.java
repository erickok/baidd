package nl.uu.cs.arg.platform;

import nl.uu.cs.arg.shared.dialogue.DialogueMessage;

/**
 * A message that indicates that the platform has either started
 * execution or stopped running.
 * 
 * @author erickok
 */
public class PlatformStateMessage extends DialogueMessage {

	private boolean nowRunning;
	
	public PlatformStateMessage(boolean nowRunning) {
		super((nowRunning? "The platform is now running.": "The platform is now paused."));
		this.nowRunning = nowRunning;
	}

	/**
	 * Returns an indication of the platform's new state
	 * @return True is the platform just started running; false if it just stopped
	 */
	public boolean isPlatformNowRunning() {
		return nowRunning;
	}
	
}
