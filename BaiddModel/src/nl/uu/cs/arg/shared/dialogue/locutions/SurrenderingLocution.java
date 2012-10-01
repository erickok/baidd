package nl.uu.cs.arg.shared.dialogue.locutions;

import java.util.List;

import org.aspic.inference.Constant;

/**
 * A class wrapper to identify a surrendering locution, according
 * to the communication language.
 * 
 * It has no additional functionality over {@link Locution}.
 * 
 * @author erickok
 *
 */
public abstract class SurrenderingLocution extends DeliberationLocution {

	public SurrenderingLocution(String name) {
		super(name);
	}

	/**
	 * Returns null, since surrendering replies never expose new beliefs
	 */
	@Override
	public List<Constant> getPublicBeliefs() {
		return null;
	}
	
}
