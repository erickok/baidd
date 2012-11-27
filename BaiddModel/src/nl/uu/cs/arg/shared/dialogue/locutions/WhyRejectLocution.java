package nl.uu.cs.arg.shared.dialogue.locutions;

import java.util.Set;

import nl.uu.cs.arg.shared.dialogue.Proposal;

import org.aspic.inference.Constant;

/**
 * The why-reject(P) locution is used to question the rejection of a 
 * proposal by some agent, where P refers to the original proposal that
 * as rejected.
 * 
 * @author erickok
 *
 */
public class WhyRejectLocution extends AttackingLocution {

	private static final String LOCUTION_NAME = "why-reject";
	
	/**
	 * The proposal that was rejected
	 */
	private Proposal rejectedProposal;
	
	public WhyRejectLocution(Proposal rejectedProposal) {
		super(LOCUTION_NAME);
		this.rejectedProposal = rejectedProposal;
	}

	/**
	 * Returns the proposal that was originally proposed and rejected in a reject(P) locution and now questioned with a why-reject(P)
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
	 * No beliefs added, since questioning rejections never expose new beliefs
	 */
	@Override
	public void gatherPublicBeliefs(Set<Constant> exposedBeliefs) {
	}

}
