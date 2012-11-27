package nl.uu.cs.arg.shared.dialogue.locutions;

import java.util.Set;

import org.aspic.inference.Constant;

/**
 * A why(p) locution, that attacks an argue move. The p is an element
 * in the A of a targeted argue(A => q) move.
 * 
 * @author erickok
 *
 */
public final class WhyLocution extends AttackingLocution {

	private static final String LOCUTION_NAME = "why";
	
	/**
	 * The term that is attacked, which is a premise used in the 
	 * argument of the argue move. 
	 */
	private Constant attackedPremise;
	
	public WhyLocution(Constant constant) {
		super(LOCUTION_NAME);
		this.attackedPremise = constant;
	}
	
	/**
	 * Returns the attacked premise as used in the argument of the argue move it replied to
	 * @return The term that was attacked
	 */
	public Constant getAttackedPremise() {
		return this.attackedPremise;
	}

	@Override
	public String toLogicString() {
		return getName() + "(" + getAttackedPremise().inspect() + ")";
	}

	/**
	 * No beliefs added, since questioning never expose new beliefs
	 */
	@Override
	public void gatherPublicBeliefs(Set<Constant> exposedBeliefs) {
	}

}
