package nl.uu.cs.arg.shared.dialogue.locutions;

import java.util.List;

import org.aspic.inference.Constant;

import nl.uu.cs.arg.shared.dialogue.Proposal;

/**
 * The reject(P) locution is used to reject a proposal that is made (in
 * contrast with questioning it using why-propose). It may start a 
 * side-branch of arguments. The P should be an already existing proposal.
 * 
 * @author erickok
 *
 */
public class RejectLocution extends AttackingLocution {

	private static final String LOCUTION_NAME = "reject";
	
	/**
	 * The proposal that is rejected
	 */
	private Proposal rejectedProposal;
	
	public RejectLocution(Proposal rejectedProposal) {
		super(LOCUTION_NAME);
		this.rejectedProposal = rejectedProposal;
	}

	/**
	 * Returns the proposal that is rejected with this reject locution
	 * @return The attacked proposal object
	 */
	public Proposal getRejectedProposal() {
		return this.rejectedProposal;
	}
	
	@Override
	public String toLogicString() {
		return getName() + "(" + getRejectedProposal().inspect() + ")";
	}

	/**
	 * Returns null, since rejecting never expose new beliefs
	 */
	@Override
	public List<Constant> getPublicBeliefs() {
		return null;
	}

}
