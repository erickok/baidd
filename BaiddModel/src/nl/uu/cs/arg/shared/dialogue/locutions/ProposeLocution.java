package nl.uu.cs.arg.shared.dialogue.locutions;

import org.aspic.inference.Constant;
import java.util.Arrays;
import java.util.List;

/**
 * A propose(P) move where P is a proposal that should respect the 
 * topic of the dialogue that we are in.
 * 
 * @author erickok
 *
 */
public class ProposeLocution extends DeliberationLocution {

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

	@Override
	public List<Constant> getPublicBeliefs() {
		return Arrays.asList((Constant)concreteProposal);
	}

}
