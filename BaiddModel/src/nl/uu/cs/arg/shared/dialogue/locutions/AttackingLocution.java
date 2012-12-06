package nl.uu.cs.arg.shared.dialogue.locutions;

/**
 * A class wrapper to identify an attacking locution, according
 * to the communication language.
 * 
 * It has no additional functionality over {@link Locution}.
 * 
 * @author erickok
 *
 */
public abstract class AttackingLocution extends ProposalRelatedLocution {

	public AttackingLocution(String name) {
		super(name);
	}

}
