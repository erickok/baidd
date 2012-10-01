package nl.uu.cs.arg.shared.dialogue.locutions;

import java.util.List;

import org.aspic.inference.Constant;

/**
 * A locution class wrapper to identify locution used exclusively 
 * during the deliberation process of the dialogue. Useful for 
 * method that want to test the locutoin type via instanceof.
 * 
 * It has no additional functionality over {@link Locution}.
 * 
 * @author erickok
 *
 */
public abstract class DeliberationLocution extends Locution {

	public DeliberationLocution(String name) {
		super(name);
	}

	/**
	 * Get the list of elements (constants, terms) that this locution exposes
	 * @return
	 */
	public abstract List<Constant> getPublicBeliefs();
	
}
