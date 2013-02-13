package nl.uu.cs.arg.platform.local;

import java.util.List;

import nl.uu.cs.arg.shared.Agent;
import nl.uu.cs.arg.shared.Participant;
import nl.uu.cs.arg.shared.dialogue.DialogueException;
import nl.uu.cs.arg.shared.dialogue.DialogueMessage;
import nl.uu.cs.arg.shared.dialogue.Move;
import nl.uu.cs.arg.shared.dialogue.locutions.JoinDialogueLocution;
import nl.uu.cs.arg.shared.dialogue.locutions.Locution;
import nl.uu.cs.arg.shared.dialogue.locutions.OpenDialogueLocution;

/**
 * The InactiveAgent is a fully implemented yet totally inactive agent.
 * Apart from joining the dialogue, it will always skip turn and does 
 * nothing with incoming moves and messages.
 * 
 * @author erickok
 *
 */
public class InactiveAgent implements Agent {

	private static final String NAME = "Inactive agent";
	private String name = NAME;
	private Participant participant;
	
	/**
	 * Create the agent from an XML data specification; this inactive agent 
	 * will only use the provided name and discard all other data
	 * @param xmlDataFile The raw XML data
	 */
	public InactiveAgent(AgentXmlData xmlDataFile) {
		name = xmlDataFile.getName();
	}

	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public void initialize(Participant participant) {
		this.participant = participant;
	}

	@Override
	public Move<? extends Locution> decideToJoin(OpenDialogueLocution openDialogue) {
		// Always join the dialogue
		Move<JoinDialogueLocution> join = Move.buildMove(participant, null, new JoinDialogueLocution(openDialogue.getTopic()));
		return join;
	}

	@Override
	public List<Move<? extends Locution>> makeMoves() {
		// Always skip the turn
		return null;
	}

	@Override
	public void onNewMovesReceived(List<Move<? extends Locution>> moves) {
		// Do nothing
	}

	@Override
	public void onDialogueException(DialogueException e) {
		// Do nothing
	}

	@Override
	public void onDialogueMessagesReceived(List<? extends DialogueMessage> messages) {
		// Do nothing
	}

	public String toString() {
		return getName();
	}
	
}
