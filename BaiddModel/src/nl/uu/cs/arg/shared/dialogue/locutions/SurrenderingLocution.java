package nl.uu.cs.arg.shared.dialogue.locutions;

import java.util.Set;

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
public abstract class SurrenderingLocution extends ProposalRelatedLocution {

	public SurrenderingLocution(String name) {
		super(name);
	}

	/**
	 * No belief added, since surrendering replies never expose new beliefs
	 */
	@Override
	public void gatherPublicBeliefs(Set<Constant> exposedBeliefs) {
	}
	
}
