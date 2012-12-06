package nl.uu.cs.arg.shared.dialogue.locutions;

import java.util.Set;

import org.aspic.inference.Constant;

/**
 * A propose(P) move where P is a proposal that should respect the 
 * topic of the dialogue that we are in.
 * 
 * @author erickok
 *
 */
public class ProposeLocution extends ProposalRelatedLocution {

	private static final String LOCUTION_NAME = "propose";

	/**
	 * The proposal that is moved with this locution
	 */
	private Constant concreteProposal;
	
	public ProposeLocution(Constant constant) {
		super(LOCUTION_NAME);
		this.concreteProposal = constant;
	}

	/**
	 * The actual proposal that is moved, which respects the dialogue topic
	 * @return The contained proposal
	 */
	public Constant getConcreteProposal() {
		return this.concreteProposal;
	}
	
	/**
	 * Returns a string of the form 'propose(P)' where P is the actual proposal
	 * @return A formatted and human-readable string
	 */
	@Override
	public String toLogicString() {
		return getName() + "(" + getConcreteProposal().inspect() + ")";
	}

	/**
	 * Adds the proposed option as exposed belief
	 */
	@Override
	public void gatherPublicBeliefs(Set<Constant> exposedBeliefs) {
		exposedBeliefs.add(concreteProposal);
	}

}
