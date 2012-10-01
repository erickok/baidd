package nl.uu.cs.arg.shared.dialogue.protocol;

import nl.uu.cs.arg.shared.dialogue.Dialogue;

/**
 * A termination rule determines whether the ongoing deliberation 
 * dialogue should terminate. This may depend on several factors,
 * such as the dialogue state or the number of participants. The 
 * platform can instantiate multiple of these termination
 * rules, depending on its settings.
 * 
 * @author erickok
 *
 */
public enum TerminationRule {

	/**
	 * Terminate if there are no (more) participants
	 */
	NoParticipants {
		@Override
		public TerminationMessage shouldTerminate(Dialogue dialogue, int participants, int skips) {
			if (participants <= 0) {
				return new TerminationMessage("No (more) participants.");
			}
			return null;
		}
		
	},
	/**
	 * Terminate if the last X moves, where X is the number of participants + 1, were skips
	 */
	InactiveRound {
		@Override
		public TerminationMessage shouldTerminate(Dialogue dialogue, int participants, int skips) {
			if (skips >= participants + 1) {
				return new TerminationMessage("The dialogue was inactive for a full round: All of the agents skipped turn and the first skipped twice.");
			}
			return null;
		}
	};

	/**
	 * Evaluates the dialogue on whether it should terminate
	 * @param dialogue The current state of the dialogue
	 * @param participants The number of participants in the dialogue
	 * @param skips The number of subsequent skips (non-moving) by the participating agents
	 * @return The reason, in text, why the dialogue should terminate; or null if it should not 
	 */
	public abstract TerminationMessage shouldTerminate(Dialogue dialogue, int participants, int skips);
	
}
