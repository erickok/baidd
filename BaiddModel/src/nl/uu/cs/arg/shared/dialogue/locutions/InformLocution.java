package nl.uu.cs.arg.shared.dialogue.locutions;

import java.util.Set;

import org.aspic.inference.Constant;

/**
 * An inform(p) move, where p is a belief (rule, term, constant). Inform moves have no explicit target.
 * 
 * @author erickok
 */
public class InformLocution extends DeliberationLocution {

	private static final String LOCUTION_NAME = "inform";

	/**
	 * The informed belief (rule, constant, term)
	 */
	private Constant belief;
	
	public InformLocution(Constant belief) {
		super(LOCUTION_NAME);
		this.belief = belief;
	}

	/**
	 * The informed belief
	 * @return The belief, which is a rule, term or constant
	 */
	public Constant getBelief() {
		return this.belief;
	}
	
	/**
	 * Returns a string of the form 'inform(p)' where p is a single belief
	 * @return A formatted and human-readable string
	 */
	@Override
	public String toLogicString() {
		return getName() + "(" + belief.inspect() + ")";
	}

	/**
	 * Adds the informed belief as exposed belief
	 */
	@Override
	public void gatherPublicBeliefs(Set<Constant> exposedBeliefs) {
		exposedBeliefs.add(belief);
	}

}
