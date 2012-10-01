package nl.uu.cs.arg.shared.dialogue.locutions;

import java.util.List;

import org.aspic.inference.Constant;

import nl.uu.cs.arg.shared.dialogue.Proposal;

/**
 * The why-propose(P) locution is used to attack a proposal if an agents
 * requires support for P, which is a previously submitted proposal.
 * 
 * @author erickok
 *
 */
public class WhyProposeLocution extends AttackingLocution {

	private static final String LOCUTION_NAME = "why-propose";
	
	/**
	 * The proposal that is attacked
	 */
	private Proposal targetProposal;
	
	public WhyProposeLocution(Proposal targetProposal) {
		super(LOCUTION_NAME);
		this.targetProposal = targetProposal;
	}

	/**
	 * Returns the proposal that is attacked with this why-propose locution
	 * @return The attacked proposal object
	 */
	public Proposal getAttackedProposal() {
		return this.targetProposal;
	}
	
	@Override
	public String toLogicString() {
		return getName() + "(" + getAttackedProposal().inspect() + ")";
	}

	/**
	 * Returns null, since questioning proposals never expose new beliefs
	 */
	@Override
	public List<Constant> getPublicBeliefs() {
		return null;
	}

}
