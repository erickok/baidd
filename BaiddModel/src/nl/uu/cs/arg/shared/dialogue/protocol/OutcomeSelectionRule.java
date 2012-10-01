package nl.uu.cs.arg.shared.dialogue.protocol;

import java.util.ArrayList;
import java.util.Collections;

import nl.uu.cs.arg.shared.dialogue.Dialogue;
import nl.uu.cs.arg.shared.dialogue.Proposal;

public enum OutcomeSelectionRule {

	FirstThatIsIn {
		@Override
		public Proposal determineOutcome(Dialogue dialogue) {
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
		public Proposal determineOutcome(Dialogue dialogue) {
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
	
	public abstract Proposal determineOutcome(Dialogue dialogue);
	
}
