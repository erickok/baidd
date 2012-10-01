package nl.uu.cs.arg.shared.dialogue.locutions;


import org.aspic.inference.Term;

/**
 * A join-dialogue(t) move, where t is the dialogue topic. In the framework
 * this is a reply to an open-dialogue(t, g) move by some other agents.
 * 
 * @author erickok
 *
 */
public final class JoinDialogueLocution extends Locution {

	private static final String LOCUTION_NAME = "join-dialogue";

	/**
	 * The term describing the topic of the dialogue that we want to join
	 */
	private Term topic;

	public JoinDialogueLocution(Term topic) {
		super(LOCUTION_NAME);
		this.topic = topic;
	}
	
	/**
	 * Returns the topic of the dialogue that we indicate we which to join with 
	 * this locution
	 * @return A term describing the dialogue topic
	 */
	public Term getTopic() {
		return this.topic;
	}

	/**
	 * Returns a string of the form 'join-dialogue(t)' where t is the topic goal
	 * @return A formatted and human-readable string
	 */
	@Override
	public String toLogicString() {
		return getName() + "(" + getTopic().inspect() + ")";
	}

}
