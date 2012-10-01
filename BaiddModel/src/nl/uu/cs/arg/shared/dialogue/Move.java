package nl.uu.cs.arg.shared.dialogue;

import nl.uu.cs.arg.shared.Participant;
import nl.uu.cs.arg.shared.dialogue.locutions.Locution;
import nl.uu.cs.arg.shared.util.IndexedObject;

/**
 * A Move is a single move in a dialogue played by an agent. It is defined 
 * by a {@link Locution}, which also contains the contents, and meta-data 
 * such as the id and target move. It cannot be instantiated directly, but 
 * only via the buildMove method.
 * 
 * @author erickok
 *
 */
public class Move<T extends Locution> implements IndexedObject {

	/**
	 * A counter used to create unique move identifiers; buildMove() will use and increment this
	 */
	private static int uniqueMoveCounter = 0;
	
	/**
	 * The internal, dialogue-unique identifier for a single move.
	 */
	private long index;
	
	/**
	 * The agent that played this move.
	 */
	private Participant player;
	
	/**
	 * The {@link Move} that this move is a reply to.
	 */
	private Move<? extends Locution> target;
	
	/**
	 * The locution played in this move, which contains the contents as 
	 * well, such as the actual {@link Argument}.
	 */
	private T locution;
	
	/**
	 * Returns the internal, dialogue-unique identifier for a single move
	 * @return The move id
	 */
	public long getIndex() {
		return this.index;
	}
	
	/**
	 * Returns the player of this move
	 * @return The participant object of the agent that player this move
	 */
	public Participant getPlayer() {
		return this.player;
	}
	
	/**
	 * Returns the target move to which this move is a reply to
	 * @return The targeted move, or null if it had no target (e.g. an join-dialogue move)
	 */
	public Move<? extends Locution> getTarget() {
		return this.target;
	}
	
	/**
	 * Return the wrapped locution in this dialogue move
	 * @return The locution object that was played with this move
	 */
	public T getLocution() {
		return this.locution;
	}
	
	/**
	 * Internal, default constructor
	 * @param id The unique move id
	 * @param player The participant that plays this move
	 * @param target The move this is a reply to (or null)
	 * @param locution The move contents, which is a locution of some sort
	 */
	private Move(int id, Participant player, Move<? extends Locution> target, T locution) {
		this.index = id;
		this.player = player;
		this.target = target;
		this.locution = locution;
	}
	
	/**
	 * Create a new Move to be submitted to the dialogue. This also assigns a 
	 * unique ID and creates the internal locution, which details can be 
	 * assigned afterwards
	 * @param player The participant that plays this move
	 * @param targetMove The move this is a reply to (or null)
	 * @param locutionType The type of locution that will be contained in this move
	 * @return The constructed Move, of which its locution is instantiated, but should still be assigned details to (e.g. the attacked premise of a why locution)
	 */
	public static <T extends Locution> Move<T> buildMove(Participant player, Move<? extends Locution> targetMove, T locution) {
		// Note that the move id counter is incremented (after the Move object was instantiated)
		return new Move<T>(uniqueMoveCounter++, player, targetMove, locution);
	}
	
	/**
	 * Resets the internal move counter; to be used when starting a new dialogue (platform)
	 */
	public static void resetMoveCounter() {
		uniqueMoveCounter = 0;
	}

	/**
	 * Returns a string representing this move's contents in the form '(moveid) playername: locution(contents) --> targetmoveid'
	 * @return A formatted and human-readable string of this move
	 */
	public String toLogicString() {
		return "(" + index + ") " + (player == null? "<system>": player.getName()) + ": " + locution.toLogicString() + (target != null? " --> " + target.getIndex(): "");
	}
	
	public String toString() {
		return this.toLogicString();
	}
	
}
