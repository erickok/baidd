package nl.uu.cs.arg.shared.dialogue;

import java.util.ArrayList;
import java.util.List;

import nl.uu.cs.arg.shared.Participant;
import nl.uu.cs.arg.shared.dialogue.locutions.ArgueLocution;
import nl.uu.cs.arg.shared.dialogue.locutions.AttackingLocution;
import nl.uu.cs.arg.shared.dialogue.locutions.ConcedeLocution;
import nl.uu.cs.arg.shared.dialogue.locutions.Locution;
import nl.uu.cs.arg.shared.dialogue.locutions.ProposeLocution;
import nl.uu.cs.arg.shared.dialogue.locutions.SurrenderingLocution;
import nl.uu.cs.arg.shared.util.IndexedNode;
import nl.uu.cs.arg.shared.util.IndexedTree;

/**
 * A Proposal represents a concrete proposal as put forward by one of
 * the participating agents. In addition, it contains the tree of moves
 * that represent the ongoing dialogue.
 * 
 * Proposals need to be instantiated with the move in which they were
 * originally proposed; this will be used as the root element of the 
 * tree.
 *  
 * @author erickok
 *
 */
public class Proposal extends IndexedTree<Move<? extends Locution>> {

	/**
	 * Internal constructor (which also initializes the moves tree)
	 * @param originator The agent that originally made this proposal
	 * @param proposal The contents of the proposal that was moved
	 */
	public Proposal(Move<ProposeLocution> proposeMove) {
		super();
		this.setRootElement(new IndexedNode<Move<? extends Locution>>(this, proposeMove));
	}
	
	/**
	 * Returns the original propose move by which this proposal was submitted
	 * @return The propose move, which contains the propose locution
	 */
	@SuppressWarnings("unchecked")
	public Move<ProposeLocution> getProposalMove() {
		return (Move<ProposeLocution>) this.getRootElement().getData();
	}
	
	/**
	 * Returns the locution in which the original proposal was made
	 * @return The original propose locution
	 */
	public ProposeLocution getProposalLocution() {
		return getProposalMove().getLocution();
	}
	
	/**
	 * Returns details on the agents that originally submitted this proposal
	 * @return The participant object of the originator
	 */
	/*public Participant getOriginator() {
		return getProposalMove().getPlayer();
	}*/
	
	/**
	 * Add a new move (with specified target) to the proposal tree - attacking the right node
	 * @param newMove The move to add to the tree
	 * @return Returns the {@link IndexedNode} that the newMove was added to; or null if the target was not in this proposal tree
	 */
	public IndexedNode<Move<? extends Locution>> addMoveNode(Move<? extends Locution> newMove) {
		
		// Find the newMove's target
		IndexedNode<Move<? extends Locution>> target = findNodeByIndex(newMove.getTarget().getIndex());
		if (target == null) {
			// Not in this proposal tree
			return null;
		}
		
		// Found the target; add a node for this newMove to its children
		target.addChild(new IndexedNode<Move<? extends Locution>>(this, newMove));
		return target;
		
	}
	
	/**
	 * Returns all the replies to some move in this proposal tree
	 * @param move A move to return all it's replies for
	 * @return A list of all the moves that were replies to the given move
	 * @throws DialogueException Thrown when the replies are asked for some move that is not in this proposal's tree
	 */
	public List<Move<? extends Locution>> getReplies(Move<? extends Locution> move) throws DialogueException {
		IndexedNode<Move<? extends Locution>> node = findNodeByIndex(move.getIndex());
		if (node == null) {
			throw new DialogueException("Asked for the replies of '" + move.toLogicString() + "', but it is not present in this proposal tree.");
		}
		List<Move<? extends Locution>> replies = new ArrayList<Move<? extends Locution>>();
		for (IndexedNode<Move<? extends Locution>> child : node.getChildren()) {
			replies.add(child.getData());
		}
		return replies;
	}
	
	/**
	 * This evaluates the dialogical status of this proposal, which in effect
	 * is the dialogical status of the move in which it was proposed. See
	 * isIn(Move<?> move) for the definition of 'in' and 'out'. 
	 * @return True if this proposal is 'in'
	 */
	public boolean isIn() {
		try {
			return isIn(getRootElement().getData());
		} catch (DialogueException e) {
			return false;
		}
	}
	
	/**
	 * This evaluates the dialogical status of some move (which should be played 
	 * inside this proposal tree); a move m in dialogue d is <i>in</i> iff (1) m 
	 * is surrendered to by all agents that attacked this move; or else (2) all 
	 * attacking replies to m are <i>out</i>.
	 * @param move The move to evaluate for its dialogical status
	 * @return True if the move is <i>in</i>
	 * @throws DialogueException Thrown when the dialogical status of some move is asked that is not inside this proposal
	 */
	public boolean isIn(Move<? extends Locution> move) throws DialogueException {
		
		IndexedNode<Move<? extends Locution>> node = findNodeByIndex(move.getIndex());
		if (node == null) {
			throw new DialogueException("Asked for the dialectical status of '" + move.toLogicString() + "', but it is not present in this proposal tree.");
		}
		
		// Surrendering replies are always out
		if (node.getData().getLocution() instanceof SurrenderingLocution) {
			return false;
		}

		boolean isIn = true;
		List<Participant> attackers = new ArrayList<Participant>();
		
		// Check for attacking replies
		for (IndexedNode<Move<? extends Locution>> reply : node.getChildren()) {
			if (reply.getData().getLocution() instanceof AttackingLocution && this.isIn(reply.getData())) {
				// Found an attacker that is 'in' (not considering the surrenders)
				isIn = false;
				attackers.add(reply.getData().getPlayer());
			}
		}
		
		// Check for surrendering replies by agents that also attacked this move 
		if (!isIn) {
			for (IndexedNode<Move<? extends Locution>> reply : node.getChildren()) {
				if (reply.getData().getLocution() instanceof SurrenderingLocution) {
					
					// Does it fully surrender to the targeting move?
					// Only concede moves may not surrender to the whole move but only a part
					if ((reply.getData().getLocution() instanceof ConcedeLocution &&
							((ConcedeLocution)reply.getData().getLocution()).getConcededTerm().equals(((ArgueLocution)move.getLocution()).getArgument().getClaim())) || 
							!(reply.getData().getLocution() instanceof ConcedeLocution)) {

							// Surrendered by an agent that attacked this move; then remove this from the original attackers list
							attackers.remove(reply.getData().getPlayer());
							// If no attacking agents are left, this move is 'in' again
							if (attackers.size() <= 0) {
								isIn = true;
								break;
							}
							
					}
					
				}
			}
		}
		
		return isIn;
		
	}
	
	/**
	 * Returns the list of active attackers in this proposal tree; moves 
	 * that are concrete causes for this proposal to be 'out'
	 * @return A list of all moves in this tree that are active attackers
	 */
	public List<Move<? extends Locution>> getActiveAttackers() {
		List<Move<? extends Locution>> attackers = new ArrayList<Move<? extends Locution>>();
		try {

			// Find all active attackers in this dialogue (including the propose move, if it is 'in') 
			fillActiveAttackersList(attackers, getRootElement(), true);
			
		} catch (DialogueException e) {
			e.printStackTrace();
		}
		return attackers;
	}
	
	private void fillActiveAttackersList(List<Move<? extends Locution>> attackers, IndexedNode<Move<? extends Locution>> node, boolean parentIsActiveAttacker) throws DialogueException {

		// Add every attacking reply that is 'in'
		if ((node.getData().getLocution() instanceof ProposeLocution || 
				node.getData().getLocution() instanceof AttackingLocution) && isIn(node.getData())) {
			
			attackers.add(node.getData());

			// For active attackers, also look into its children for active attackers
			for (IndexedNode<Move<? extends Locution>> reply : node.getChildren()) {
				fillActiveAttackersList(attackers, reply, true);
			}
			
		} else if (parentIsActiveAttacker) {

			// For active attackers, also look into its children for active attackers
			for (IndexedNode<Move<? extends Locution>> reply : node.getChildren()) {
				fillActiveAttackersList(attackers, reply, false);
			}
			
		}
		
		
	}
	
	/**
	 * Returns a string indication the concrete proposal contents, which is the inspect() on the actual Term
	 * @return A formatted and human-readable string
	 */
	public String inspect() {
		return getProposalLocution().getConcreteProposal().inspect();
	}

	public String toString() {
		return super.toString();
	}
	
}
