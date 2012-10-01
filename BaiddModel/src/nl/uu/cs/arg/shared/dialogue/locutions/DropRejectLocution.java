package nl.uu.cs.arg.shared.dialogue.locutions;

import nl.uu.cs.arg.shared.dialogue.Proposal;

/**
 * The drop-reject(P) locution is used to drop a reject(P) locution.
 * The P that was proposed was attack with some reject locution, but 
 * this drop_reject makes it obsolete. In effect, the original 
 * proposal P is no longer attacked by the reject(P) move.
 * 
 * @author erickok
 *
 */
public class DropRejectLocution extends SurrenderingLocution {

	private static final String LOCUTION_NAME = "drop-reject";
	
	/**
	 * The proposal that was originally rejected
	 */
	private Proposal rejectedProposal;
	
	public DropRejectLocution(Proposal rejectedProposal) {
		super(LOCUTION_NAME);
		this.rejectedProposal = rejectedProposal;
	}

	/**
	 * Returns the proposal that was originally proposed and then rejected in a reject(P) locution
	 * @return The attacked proposal object
	 */
	public Proposal getRejectedProposal() {
		return this.rejectedProposal;
	}
	
	@Override
	public String toLogicString() {
		return getName() + "(" + getRejectedProposal().inspect() + ")";
	}

}
