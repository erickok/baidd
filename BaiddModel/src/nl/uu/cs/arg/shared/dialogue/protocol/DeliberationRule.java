package nl.uu.cs.arg.shared.dialogue.protocol;

import nl.uu.cs.arg.shared.dialogue.Dialogue;
import nl.uu.cs.arg.shared.dialogue.Move;
import nl.uu.cs.arg.shared.dialogue.locutions.Locution;

/**
 * A deliberation rule determines whether a move that is submitted
 * by an agent is valid. Different of these rules can be 
 * instantiated by a platform. Together, these rules form the 
 * dialogue protocol.
 * 
 * @author erickok
 *
 */
public enum DeliberationRule {

	/**
	 * Returns an exception if the player made an attack on one's own move
	 */
	AttackOnOwnMove {
		@Override
		public ProtocolException evaluateMove(Dialogue dialogue, Move<? extends Locution> newMove) {
			if (newMove != null && newMove.getTarget() != null && newMove.getTarget().getPlayer() == newMove.getPlayer()) {
				return new ProtocolException(newMove, "The new move attacks one's own: An agent may only attack moves of other players");
			}
			return null;
		}
	},
	NoRepeatInBranch {
		@Override
		public ProtocolException evaluateMove(Dialogue dialogue, Move<? extends Locution> newMove) {
			// TODO: Implement it (using proposal's .isRepeatedMove or something)
			return null;
		}
	};
	
	/**
	 * Evaluates whether a submitted move in the dialogue is allowed
	 * @param dialogue The current state of the dialogue
	 * @param move The new move that was submitted
	 * @return The reason why this move was fallacious; or null if it wasn't 
	 */
	public abstract ProtocolException evaluateMove(Dialogue dialogue, Move<? extends Locution> newMove);
	
}
