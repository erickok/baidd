package nl.uu.cs.arg.shared;

import java.util.List;

import nl.uu.cs.arg.shared.dialogue.Dialogue;
import nl.uu.cs.arg.shared.dialogue.DialogueException;
import nl.uu.cs.arg.shared.dialogue.DialogueMessage;
import nl.uu.cs.arg.shared.dialogue.Move;
import nl.uu.cs.arg.shared.dialogue.locutions.Locution;
import nl.uu.cs.arg.shared.dialogue.locutions.OpenDialogueLocution;

/**
 * Every agent build for the argumentation platform will need to implement
 * this Agent interface. A registered agent instance will get its methods
 * called through this interface definition. This way lower level functions
 * like registering with the platform and waiting for turns is hidden 
 * form the agent implementation.
 * 
 * @author erickok
 *
 */
public interface Agent {

	/**
	 * This should return how the agent wants to be called in the dialogue
	 * @return The agent name
	 */
	public String getName();
	
	/**
	 * An agent that needs to initialize itself can do so here. It is only 
	 * called once per session. This is the place to initialize its reasoning
	 * module or even create external connections.
	 * @param participant The {@link Participant} object used to represent this agent in a {@link Dialogue} structure
	 */
	public void initialize(Participant participant);
	
	/**
	 * The dialogue is starting and the agent should here decide whether it
	 * wants to join the dialogue. It should return either a deny-dialogue
	 * or join-dialogue move.
	 * @param openDialogue The open-dialogue location containing the deliberation topic and topic goal
	 */
	public Move<? extends Locution> decideToJoin(OpenDialogueLocution openDialogue);
	
	/**
	 * An agent implementation will have the chance here to make moves in 
	 * an ongoing dialogue. It is a full single turn, so a single list of 
	 * all the desired moves has to be returned at once. This includes both
	 * new proposals and reactions to other moves.
	 * @return The moves the agents wants to make this turn, or null to skip the turn
	 */
	public List<Move<? extends Locution>> makeMoves();
	
	/**
	 * When some agent makes new moves in the dialogue, these will be 
	 * broadcasted to all other participants (including the sender). An agent
	 * will probably want to use this to update its model of the dialogue. 
	 * @param moves The new moves that were submitted to the dialogue by some participant
	 */
	public void onNewMovesReceived(List<Move<? extends Locution>> moves);

	/**
	 * When some messages were received from the platform, an agent may be 
	 * notified of this (e.g. when the dialogue terminates). In most cases
	 * the agent will want to look at the supertype to determine the type 
	 * of the message.
	 * @param messages The messages about the dialogue, with textual information
	 */
	public void onDialogueMessagesReceived(List<? extends DialogueMessage> messages);

	/**
	 * When some exception occurs in the ongoing dialogue, an agent may be 
	 * notified of this. For example when it returns invalid moves.
	 * @param e The exception that was thrown, with information on the cause and the effects
	 */
	public void onDialogueException(DialogueException e);
	
}
