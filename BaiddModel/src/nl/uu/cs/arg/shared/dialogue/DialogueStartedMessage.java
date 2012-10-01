package nl.uu.cs.arg.shared.dialogue;

import org.aspic.inference.Term;

/**
 * A dialogue message indicating that the dialogue started and what
 * the dialogue topic and mutual goal are.
 *  
 * @author erickok
 *
 */
public class DialogueStartedMessage extends DialogueMessage {

	/**
	 * The topic of the dialogue, which is a non-negative Term that needs
	 * to be unified in the concrete proposals.
	 */
	private Term topic;
	
	/**
	 * The topic goal. Every participating agents will need to adopt and 
	 * respect it.
	 */
	private Goal topicGoal;
	
	public DialogueStartedMessage(Term topic, Goal topicGoal, String message) {
		super(message);
		this.topic = topic;
		this.topicGoal = topicGoal;
	}

	public DialogueStartedMessage(Term topic, Goal topicGoal) {
		this(topic, topicGoal, "Dialogue on " + topic.inspect() + " started with mutual goal " + topicGoal.inspect());
	}

	/**
	 * Returns the dialogue topic
	 * @return The topic, which is a non-negative Term that needs to be unified in the concrete proposals
	 */
	public Term getTopic() {
		return this.topic;
	}
	
	/**
	 * Return this dialogue's topic goal
	 * @return The topic goal, which contains a Term representing the mutual goal to be respected in the dialogue
	 */
	public Goal getTopicGoal() {
		return this.topicGoal;
	}
	
}
