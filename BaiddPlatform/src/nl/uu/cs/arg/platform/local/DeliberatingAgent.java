package nl.uu.cs.arg.platform.local;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.uu.cs.arg.shared.Agent;
import nl.uu.cs.arg.shared.Participant;
import nl.uu.cs.arg.shared.dialogue.Dialogue;
import nl.uu.cs.arg.shared.dialogue.DialogueException;
import nl.uu.cs.arg.shared.dialogue.DialogueMessage;
import nl.uu.cs.arg.shared.dialogue.DialogueState;
import nl.uu.cs.arg.shared.dialogue.DialogueStateChangeMessage;
import nl.uu.cs.arg.shared.dialogue.Goal;
import nl.uu.cs.arg.shared.dialogue.Move;
import nl.uu.cs.arg.shared.dialogue.SkipMoveMessage;
import nl.uu.cs.arg.shared.dialogue.locutions.JoinDialogueLocution;
import nl.uu.cs.arg.shared.dialogue.locutions.Locution;
import nl.uu.cs.arg.shared.dialogue.locutions.OpenDialogueLocution;

import org.aspic.inference.Constant;
import org.aspic.inference.ConstantList;
import org.aspic.inference.KnowledgeBase;
import org.aspic.inference.ReasonerException;
import org.aspic.inference.Rule;
import org.aspic.inference.RuleArgument;
import org.aspic.inference.Term;
import org.aspic.inference.parser.ParseException;

/**
 * The deliberating agent is a template for agents that implement the deliberation behaviour model that devices the move making decision into four distinct steps: knowledge revision, attitude assignment, attack point identification and move generation. It handles boilerplate code of maintaining a model of the ongoing dialogue and storing local knowledge. Moreover, the agent will always join the dialogue.
 * 
 * @author erickok
 *
 */
public abstract class DeliberatingAgent implements Agent, StrategyExposer {

	private static final String NAME = "Deliberating agent";
	protected String name = NAME;

	protected StrategyHelper helper = StrategyHelper.DefaultHelper;
	protected Participant participant;
	protected List<Participant> participants = new ArrayList<Participant>();
	protected Dialogue dialogue;
	protected int skipCount = 0;
	
	protected KnowledgeBase beliefs;
	protected List<Rule> optionBeliefs;
	protected List<Goal> goalsHidden;
	protected List<Goal> goalsPublic;
	protected Set<Constant> initialBeliefs;
	
	protected DeliberatingAgent(String name, Map<String, Object> rawProperties, KnowledgeBase beliefs, List<Rule> optionBeliefs, List<Goal> goalsHidden, List<Goal> goalsPublic) {
		this.name = name;
		this.beliefs = beliefs;
		this.optionBeliefs = optionBeliefs;
		this.goalsHidden = goalsHidden;
		this.goalsPublic = goalsPublic;
		this.initialBeliefs = new HashSet<Constant>();
		this.initialBeliefs.addAll(this.beliefs.getRules());
		this.initialBeliefs.addAll(this.optionBeliefs);
		for (Goal goal : this.goalsHidden) {
			this.initialBeliefs.add(goal.getGoalContent());
		}
		for (Goal goal : this.goalsPublic) {
			this.initialBeliefs.add(goal.getGoalContent());
		}
	}

	/**
	 * Create the agent from an XML data specification
	 * @param xmlDataFile The parsed XML data
	 */
	public DeliberatingAgent(AgentXmlData xmlDataFile) {
		this(xmlDataFile.getName(), 
				xmlDataFile.getRawProperties(), 
				xmlDataFile.getBeliefBase(), 
				xmlDataFile.getOptions(), 
				xmlDataFile.getHiddenGoals(), 
				xmlDataFile.getPublicGoals());
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
		
		// Store the dialogue (with topic and goal)
		this.dialogue = new Dialogue(openDialogue.getTopic(), openDialogue.getTopicGoal());
		this.dialogue.setState(DialogueState.Joining);
		
		// Always join the dialogue
		Move<JoinDialogueLocution> join = Move.buildMove(participant, null, new JoinDialogueLocution(openDialogue.getTopic()));
		return join;
		
	}

	@Override
	public List<Move<? extends Locution>> makeMoves() {
		
		try {

			// 1: Move evaluation
			// See onNewMovesReceived
			
			// 2: Option generation
			List<Constant> options = generateOptions();
			
			// 3: Option evaluation
			List<ValuedOption> valuedOptions = evaluateAllOptions(options);

			// 4: Option analysis
			analyseOptions(valuedOptions);
			
			// 5: Move generation
			return generateMoves(valuedOptions);
			
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		} catch (ReasonerException e) {
			e.printStackTrace();
			return null;
		} catch (DialogueException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Stores new beliefs found in the newly moved locutions (both options and facts/rules)
	 * @param moves The new moves played by some agent
	 */
	protected abstract void storeNewBeliefs(List<Move<? extends Locution>> moves) throws ParseException, ReasonerException;

	/**
	 * Considering the beliefbase and a set of goals, generate new options. 
	 * This is done by querying on the dialogue topic, considering our belief base
	 * and option beliefs and see if we can form an argument. The proof of such 
	 * an argument will contain a bottom-level rule with the concrete proposal.
	 * @return A list of all the options we can think of
	 */
	protected List<Constant> generateOptions() throws ParseException, ReasonerException {
	
		Term topic = this.dialogue.getTopic();
		List<RuleArgument> proofs = helper.findProof(new ConstantList(topic), 0.0, this.beliefs, this.optionBeliefs, null);
		List<Constant> found = new ArrayList<Constant>();
		
		// If there are arguments found, use one to create a new proposal
		for (RuleArgument proof : proofs) {
			
			// Look into the sub-arguments to get the original concrete instantiation of the topic
			// (This sub-arguments iterator is handles the recursion)
			Iterator<RuleArgument> iter = proof.subArgumentIterator();
			while (iter.hasNext()) {
				RuleArgument arg = iter.next();
				
				// If we have found the bottom-level rule, add this as the concrete proposal (but no duplicates)
				if (arg.isAtomic() && arg.getClaim() instanceof Term && arg.getClaim().isUnifiable(topic) && !found.contains(arg.getClaim())) {
					found.add((Term) arg.getClaim());
					break;
				}
			}			
			
		}
		return found;
		
	}

	/**
	 * Returns a list of valued options, where the utilities have been based on 
	 * the goals that the options satisfied.
	 * @param options The list of known options
	 * @return The list of options with an assigned utility value
	 */
	protected abstract List<ValuedOption> evaluateAllOptions(List<Constant> options) throws ParseException, ReasonerException;

	protected abstract void analyseOptions(List<ValuedOption> valuedOptions);

	protected abstract List<Move<? extends Locution>> generateMoves(List<ValuedOption> valuedOptions) throws DialogueException, ParseException, ReasonerException;

	/**
	 * Returns whether a certain belief (proposition) is an option
	 * @param b The belief to test
	 * @return True if the belief actually is an option, false otherwise
	 */
	protected boolean isBeliefInOptions(Constant b) {
		for (Rule belief : this.optionBeliefs) {
			if (belief.getConsequent().equals(b)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void onNewMovesReceived(List<Move<? extends Locution>> moves) {
		
		// Update our knowledge of the agents that are playing
		for (Move<? extends Locution> move : moves) {
			if (move.getLocution() instanceof JoinDialogueLocution) {
				participants.add(move.getPlayer());
			}
		}

		// Update the skip count
		skipCount = 0;
		
		// Update our internal dialogue model
		try {
			if (this.dialogue != null) {
				this.dialogue.update(moves);
			}
		} catch (DialogueException e) {
			// Invalid moves were played by some agent: ignore this
		}
		
		// Evaluate whether new move beliefs should be adopted in our knowledge base
		try {
			storeNewBeliefs(moves);
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (ReasonerException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void onDialogueException(DialogueException e) {
		// Do nothing
	}

	@Override
	public void onDialogueMessagesReceived(List<? extends DialogueMessage> messages) {
		for (DialogueMessage message : messages) {
			if (dialogue != null && message instanceof DialogueStateChangeMessage) {
				// Update the state of our dialogue
				dialogue.setState(((DialogueStateChangeMessage)message).getNewState());
			} else if (dialogue != null && message instanceof SkipMoveMessage) {
				// Update the skip count
				skipCount++;
			}
		}
	}

	public String toString() {
		return getName();
	}
	
	/**
	 * Returns the list of known options with their valuation based on the 
	 * utilities of the satisfied goals
	 */
	@Override
	public List<ValuedOption> getAllOptions() {
		try {
			return this.evaluateAllOptions(generateOptions());
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		} catch (ReasonerException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Returns the number of non auto-generated rules initially in our knowledge base
	 */
	@Override
	public Set<Constant> getInitialBeliefs() {
		return initialBeliefs;
	}

	/**
	 * Returns the strategy configuration, e.g. the property settings
	 */
	@Override
	public Map<String, Object> getStategyProperties() {
		return new HashMap<String, Object>();
	}
	
}
