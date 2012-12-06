package nl.uu.cs.arg.shared.dialogue.locutions;

/**
 * A locution class wrapper to identify locutions used to make a 
 * proposal or to attack or surrender in a proposal tree. Useful for 
 * method that want to test the locution type via instanceof.
 * 
 * It has no additional functionality over {@link DeliberationLocution}.
 * 
 * @author erickok
 *
 */
public abstract class ProposalRelatedLocution extends DeliberationLocution {

	public ProposalRelatedLocution(String name) {
		super(name);
	}

}
