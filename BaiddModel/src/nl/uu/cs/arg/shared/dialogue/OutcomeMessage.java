package nl.uu.cs.arg.shared.dialogue;

/**
 * A wrapper message to broadcast the dialogue outcome. This is one of 
 * the proposal that were made or null if no outcome could be 
 * established.
 * 
 * @author Eric
 *
 */
public class OutcomeMessage extends DialogueMessage {

	/**
	 * The outcome of the dialogue
	 */
	Proposal outcome;
	
	public OutcomeMessage(Proposal outcome) {
		super("The dialogue outcome is: " + (outcome == null? Dialogue.Undetermined: outcome.inspect()));
		this.outcome = outcome;
	}

	/**
	 * Returns the outcome of the deliberation dialogue
	 * @return The winning dialogue proposal, or null if none was determined
	 */
	public Proposal getOutcome() {
		return outcome;
	}
	
}
