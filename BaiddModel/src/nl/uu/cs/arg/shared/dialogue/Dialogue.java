package nl.uu.cs.arg.shared.dialogue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nl.uu.cs.arg.shared.dialogue.locutions.InformLocution;
import nl.uu.cs.arg.shared.dialogue.locutions.Locution;
import nl.uu.cs.arg.shared.dialogue.locutions.ProposalRelatedLocution;
import nl.uu.cs.arg.shared.dialogue.locutions.ProposeLocution;
import nl.uu.cs.arg.shared.util.IndexedNode;

import org.aspic.inference.Constant;
import org.aspic.inference.Term;

/**
 * A Dialogue object captures the data that defines a single argumentation
 * dialogue between agents. This means the topic and its goal, the proposals
 * and all the arguments moved. Every proposal is a tree that, on itself,
 * has an underlying Dung-style argumentation 'system'. Every proposal should
 * be an instantiation of the topic, with arguments supporting the topic 
 * goal. A Dialogue is a model object without execution behavior.
 * 
 * @author erickok
 *
 */
public class Dialogue{

	/**
	 * Used to show in human-readable text that the outcome was undetermined (null)
	 */
	public static final String Undetermined = "undetermined";

	/**
	 * The current state of this dialogue in the deliberation process.
	 */
	private DialogueState state;
	
	/**
	 * The topic of the dialogue, which is a non-negative Term that needs
	 * to be unified in the concrete proposals.
	 */
	private Term topic;
	
	/**
	 * The topic goal, as put forward by the very first open-dialogue move.
	 * Every participating agents will need to adopt and respect it.
	 */
	private Goal topicGoal;
	
	/**
	 * Every proposal that is made by the agents, which not only contains the
	 * concrete Term (without variables), but also the full tree of moves.
	 */
	private List<Proposal> proposals;

	/**
	 * The list of beliefs (rules, terms and constants) that agents exposed
	 * using inform(p) style moves.
	 */
	private Set<Constant> informedBeliefs;
	
	public Dialogue(Term topic, Goal topicGoal) {
		this.state = DialogueState.Unopened;
		this.topic = topic;
		this.topicGoal = topicGoal;
		this.proposals = new ArrayList<Proposal>();
		this.informedBeliefs = new HashSet<Constant>();
	}
	
	/**
	 * Returns the current dialogue state
	 * @return The current {@link DialogueState}
	 */
	public DialogueState getState() {
		return this.state;
	}
	
	/**
	 * Updates the current dialogue state
	 * @param newState The new active state of the dialogue
	 * @return The old dialogue state
	 */
	public DialogueState setState(DialogueState newState) {
		DialogueState oldState = this.state;
		this.state = newState;
		return oldState;
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
	
	/**
	 * Updates the dialogue model with the new moves that were made by the agents
	 * @param newMoves The new moves that were made
	 * @throws DialogueException Thrown when some change in the dialogue was requested that was faulty
	 */
	@SuppressWarnings("unchecked")
	public void update(List<Move<? extends Locution>> newMoves) throws DialogueException {
		
		// Update the dialogue structure for each of the moves (which should already be validated)
		for (Move<? extends Locution> newMove : newMoves) {
			Locution locution = newMove.getLocution();

			if (locution instanceof ProposeLocution) {
				
				// A new proposal
				addProposal((Move<ProposeLocution>) newMove);

			} else if (locution instanceof InformLocution) {

				this.informedBeliefs.add(((InformLocution)locution).getBelief());

			} else if (locution instanceof ProposalRelatedLocution) {
				
				// Every other deliberation-related move should have a target; based on this target, 
				// add it to the appropriate proposal (tree)
				IndexedNode<Move<? extends Locution>> wasAddedTo = null;
				if (newMove.getTarget() != null) {
					for (Proposal proposal : proposals) {
						wasAddedTo = proposal.addMoveNode(newMove);
						if (wasAddedTo != null) {
							// The target was found and the new move was added to its target
							break;
						}
					}
				}
				
				// If the target was not specified or not found in any proposal tree, throw an exception
				if (wasAddedTo == null) {
					throw new DialogueException("The move '" + newMove.toLogicString() + "' that was submitted does not have a target in any existing proposal tree or does not have a target specified at all!");
				}
				
			}
			
		}
		
	}
	
	private void addProposal(Move<ProposeLocution> proposeMove) throws DialogueException {
		if (this.state != DialogueState.Deliberating) {
			throw new DialogueException("Tried to add a proposal, but that cannot happen in state " + this.state.toString());
		}
		this.proposals.add(new Proposal(proposeMove));
	}

	/**
	 * Return a list of all previously made proposals
	 * @return The existing proposals in this dialogue
	 */
	public List<Proposal> getProposals() {
		return this.proposals;
	}

	/**
	 * Return a list of all previously exposed beliefs (using inform(p) moves)
	 * @return The rules, terms and constants that were exposed
	 */
	public Set<Constant> getInformedBeliefs() {
		return this.informedBeliefs;
	}

	public String prettyPrint() {
		String s = "Topic: " + this.topic.inspect() + "\n";
		s += "Goal: " + this.topicGoal.inspect() + "\n\n\n";
		for (Proposal proposal : proposals) {
			s += proposal.toString() + "\n\n";
		}
		return s;
	}
	
}
