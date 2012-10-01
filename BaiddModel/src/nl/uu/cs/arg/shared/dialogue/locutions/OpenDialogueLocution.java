package nl.uu.cs.arg.shared.dialogue.locutions;

import nl.uu.cs.arg.shared.dialogue.Goal;

import org.aspic.inference.Term;

/**
 * An open-dialogue(t, g) locution where t is the topic of the deliberation
 * dialogue that we propose to open and g is its mutual goal that agents
 * will need to respect when joining.
 * 
 * @author erickok
 *
 */
public final class OpenDialogueLocution extends Locution {

	private static final String LOCUTION_NAME = "open-dialogue";

	/**
	 * The topic of the deliberation dialogue that we propose to open
	 */
	private Term topic;
	
	/**
	 * The mutual goal that agents will need to respect when joining this dialogue
	 */
	private Goal topicGoal;
	
	public OpenDialogueLocution(Term topic, Goal topicGoal) {
		super(LOCUTION_NAME);
		this.topic = topic;
		this.topicGoal = topicGoal;
	}

	/**
	 * Returns the topic of the deliberation dialogue that we propose to open
	 * @return A term describing the dialogue topic
	 */
	public Term getTopic() {
		return this.topic;
	}
	
	/**
	 * Returns the mutual goal that agents will need to respect when joining this dialogue
	 * @return A goal that contains the term describing the mutual goal
	 */
	public Goal getTopicGoal() {
		return this.topicGoal;
	}
	
	/**
	 * Returns a string of the form 'open-dialogue(t, g)' where t is the topic goal and g is the mutual goal
	 * @return A formatted and human-readable string
	 */
	@Override
	public String toLogicString() {
		return getName() + "(" + getTopic().inspect() + ", " + getTopicGoal().inspect() + ")";
	}

}
