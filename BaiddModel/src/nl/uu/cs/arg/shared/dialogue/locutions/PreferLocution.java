package nl.uu.cs.arg.shared.dialogue.locutions;

import java.util.Set;

import org.aspic.inference.Constant;

/**
 * A prefer(P, Q) move, where P and Q are proposed options earlier in the dialogue. Prefer moves have no explicit target.
 * 
 * @author erickok
 */
public class PreferLocution extends DeliberationLocution {

	private static final String LOCUTION_NAME = "prefer";

	/**
	 * The preferred option
	 */
	private Constant preferred;

	/**
	 * The less preferred (undesirable) option
	 */
	private Constant undesirable;
	
	public PreferLocution(Constant preferred, Constant undesirable) {
		super(LOCUTION_NAME);
		this.preferred = preferred;
		this.undesirable = undesirable;
	}

	/**
	 * The preferred option
	 * @return The option that is the preferred of the two
	 */
	public Constant getPreferred() {
		return this.preferred;
	}

	/**
	 * The less preferred (undesirable) option
	 * @return The option that is less preferred of the two
	 */
	public Constant getUndesirable() {
		return this.undesirable;
	}
	
	/**
	 * Returns a string of the form 'prefer(P, Q)' where P and Q are options
	 * @return A formatted and human-readable string
	 */
	@Override
	public String toLogicString() {
		return getName() + "(" + preferred.inspect() + ", " + undesirable.inspect() + ")";
	}

	/**
	 * Adds the two options as exposed beliefs
	 */
	@Override
	public void gatherPublicBeliefs(Set<Constant> exposedBeliefs) {
		exposedBeliefs.add(preferred);
		exposedBeliefs.add(undesirable);
	}

}
