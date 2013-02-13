package nl.uu.cs.arg.shared.dialogue.locutions;

import java.util.Set;

import org.aspic.inference.Constant;

/**
 * A prefer-equal(P, Q) move, where P and Q are proposed options earlier in the dialogue. Prefer moves have no explicit 
 * target.
 * 
 * @author erickok
 */
public class PreferEqualLocution extends DeliberationLocution {

	private static final String LOCUTION_NAME = "prefer-equal";

	/**
	 * The first supplied option
	 */
	private Constant option1;

	/**
	 * The second supplied option
	 */
	private Constant option2;
	
	public PreferEqualLocution(Constant option1, Constant option2) {
		super(LOCUTION_NAME);
		this.option1 = option1;
		this.option2 = option2;
	}

	/**
	 * The first supplied option
	 * @return The option that is was supplied as first argument
	 */
	public Constant getOption1() {
		return this.option1;
	}

	/**
	 * The second supplied option
	 * @return The option that is was supplied as second argument
	 */
	public Constant getOption2() {
		return this.option2;
	}
	
	/**
	 * Returns a string of the form 'prefer-equal(P, Q)' where P and Q are options
	 * @return A formatted and human-readable string
	 */
	@Override
	public String toLogicString() {
		return getName() + "(" + option1.inspect() + ", " + option2.inspect() + ")";
	}

	/**
	 * Adds the two options as exposed beliefs
	 */
	@Override
	public void gatherPublicBeliefs(Set<Constant> exposedBeliefs) {
		exposedBeliefs.add(option1);
		exposedBeliefs.add(option2);
	}

}
