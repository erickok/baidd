package nl.uu.cs.arg.shared.dialogue;

import nl.uu.cs.arg.shared.Participant;

public class SkipMoveMessage extends DialogueMessage {

	/**
	 * The agent that skipped its turn
	 */
	Participant participant;
	
	public SkipMoveMessage(Participant participant) {
		super(participant.toString() + " skipped its turn.");
	}
	
	/**
	 * Returns the agent that skipped the turn
	 * @return The identifying participant object of the agent
	 */
	public Participant getSkippingAgent() {
		return this.participant;
	}

}
