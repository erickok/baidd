package nl.uu.cs.arg.shared.dialogue.protocol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nl.uu.cs.arg.shared.dialogue.Dialogue;
import nl.uu.cs.arg.shared.dialogue.Move;
import nl.uu.cs.arg.shared.dialogue.Proposal;
import nl.uu.cs.arg.shared.dialogue.locutions.Locution;

public enum OutcomeSelectionRule {

	FirstThatIsIn {
		@Override
		public Proposal determineOutcome(Dialogue dialogue, List<Move<? extends Locution>> allMoves) {
			// Select the first proposal that is in
			for (Proposal proposal : dialogue.getProposals()) {
				if (proposal.isIn()) {
					return proposal;
				}
			}
			return null;
		}
	},

	RandomInProposal {
		@Override
		public Proposal determineOutcome(Dialogue dialogue, List<Move<? extends Locution>> allMoves) {
			ArrayList<Proposal> proposals = new ArrayList<Proposal>(dialogue.getProposals());
			Collections.shuffle(proposals);
			// Select the first proposal that is in
			for (Proposal proposal : proposals) {
				if (proposal.isIn()) {
					return proposal;
				}
			}
			return null;
		}
	};
	
	/**
	 * Selects a proposal from those made in the dialogue as dialogue outcome
	 * @param dialogue The final, terminated dialogue
	 * @param allMoves All legal moves made by the agents, including prefer and other moves not affecting a proposal tree
	 * @return The winning proposal, or null if there is no dialogue outcome
	 */
	public abstract Proposal determineOutcome(Dialogue dialogue, List<Move<? extends Locution>> allMoves);
	
}
