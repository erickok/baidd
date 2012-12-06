package nl.uu.cs.arg.shared.dialogue.locutions;

import java.util.Set;

import org.aspic.inference.Constant;

/**
 * A locution class wrapper to identify locution used exclusively 
 * during the deliberation process of the dialogue. Useful for 
 * method that want to test the locutoin type via instanceof.
 * 
 * Deliberation locutions also need to implement the 
 * gatherPublicBeliefs method, which can be used to retrieve all
 * rules, terms and constants that are used in the concrete 
 * locution type. 
 * 
 * @author erickok
 *
 */
public abstract class DeliberationLocution extends Locution {

	public DeliberationLocution(String name) {
		super(name);
	}

	/**
	 * Get the list of elements (constants, terms and rules) that this locution exposes
	 * @param exposedBeliefs The set to which exposed beliefs should be added
	 */
	public abstract void gatherPublicBeliefs(Set<Constant> exposedBeliefs);
	
}
