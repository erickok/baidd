package nl.uu.cs.arg.platform;

import nl.uu.cs.arg.shared.Agent;
import nl.uu.cs.arg.shared.Participant;

/**
 * The glue between the {@link Participant}, which is a data structure
 * object, and the {@link Agent} which is an operational object. Use the
 * createParticipant() function to create instances.
 * 
 * @author erickok
 *
 */
public class ParticipatingAgent {

	/**
	 * Internal participant reference
	 */
	private Participant participant;
	
	/**
	 * Internal agent reference
	 */
	private Agent agent;
	
	private ParticipatingAgent(Participant participant, Agent agent) {
		this.participant = participant;
		this.agent = agent;
	}
	
	/**
	 * Creates a participant object for an agent and provides the actual coupling between the two classes
	 * @param agent The agent
	 * @param id The unique participant ID to be assigned to the agent in this dialogue
	 * @return A couple object between some {@link Agent} and its data representation as a {@link Participant}
	 */
	public static ParticipatingAgent createParticipant(Agent agent, int id) {
		return new ParticipatingAgent(new Participant(id, agent.getName()), agent);
	}
	
	public Participant getParticipant() {
		return this.participant;
	}
	
	public Agent getAgent() {
		return this.agent;
	}
	
	/**
	 * A participating agents equals another if their participant ID's match
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ParticipatingAgent) {
			return ((ParticipatingAgent) obj).getParticipant().getID() == this.getParticipant().getID();
		}
		return super.equals(obj);
	}

	public String toString() {
		return "<" + this.getParticipant().toString() + ", " + this.getAgent().toString() + ">";
	}
	
}
