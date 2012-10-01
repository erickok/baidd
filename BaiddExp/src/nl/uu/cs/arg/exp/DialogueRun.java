package nl.uu.cs.arg.exp;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import nl.uu.cs.arg.exp.result.DialogueStats;
import nl.uu.cs.arg.platform.ParticipatingAgent;
import nl.uu.cs.arg.platform.Platform;
import nl.uu.cs.arg.platform.PlatformException;
import nl.uu.cs.arg.platform.PlatformListener;
import nl.uu.cs.arg.platform.PlatformOutputPrinter;
import nl.uu.cs.arg.platform.Settings;
import nl.uu.cs.arg.platform.local.StrategyExposer;
import nl.uu.cs.arg.platform.local.ValuedOption;
import nl.uu.cs.arg.shared.Agent;
import nl.uu.cs.arg.shared.Participant;
import nl.uu.cs.arg.shared.dialogue.Dialogue;
import nl.uu.cs.arg.shared.dialogue.DialogueException;
import nl.uu.cs.arg.shared.dialogue.DialogueMessage;
import nl.uu.cs.arg.shared.dialogue.DialogueStartedMessage;
import nl.uu.cs.arg.shared.dialogue.DialogueState;
import nl.uu.cs.arg.shared.dialogue.DialogueStateChangeMessage;
import nl.uu.cs.arg.shared.dialogue.Goal;
import nl.uu.cs.arg.shared.dialogue.Move;
import nl.uu.cs.arg.shared.dialogue.OutcomeMessage;
import nl.uu.cs.arg.shared.dialogue.Proposal;
import nl.uu.cs.arg.shared.dialogue.locutions.DeliberationLocution;
import nl.uu.cs.arg.shared.dialogue.locutions.Locution;

import org.aspic.inference.Constant;
import org.aspic.inference.Term;

/**
 * Manages the running and monitoring (parsing the statistics) of a dialogue.
 * 
 * @author erickok
 */
public class DialogueRun implements PlatformListener {

	private final int runId;
	private final int configId;
	//private final Distribution distribution;
	private final DialogueMonitor monitor;
	private final Settings platformSettings;
	private final int experimentOptionsCount;
	private final PlatformOutputPrinter dialogueOutputPrinter;
	
	private Platform platform;
	private Dialogue dialogue;
	private Map<Proposal, Boolean> oldProposalStats = new HashMap<Proposal, Boolean>();
	private DialogueStats stats;
	private String agentStrategy;
	private int relevantMoves = 0;

	public DialogueRun(int runId, int configId, DialogueMonitor monitor, Settings platformSettings, 
			int experimentOptionsCount, String agentStrategy, PlatformOutputPrinter dialogueOutputPrinter) {
		this.runId = runId;
		this.configId = configId;
		//this.distribution = distribution;
		this.monitor = monitor;
		this.platformSettings = platformSettings;
		this.experimentOptionsCount = experimentOptionsCount;
		this.agentStrategy = agentStrategy;
		this.dialogueOutputPrinter = dialogueOutputPrinter;
	}

	public void start(Term topic, Goal topicGoal, List<Agent> agents) {

		// Init new dialogue
		platform = new Platform(platformSettings);
		if (dialogueOutputPrinter != null) {
			platform.addListener(dialogueOutputPrinter);
		}
		platform.addListener(this);
		platform.init(topic, topicGoal, agents);
		
		// Start the new dialogue (which runs synchronously)
		platform.setStartMode(false);
		platform.run();
		
	}
	
	/**
	 * Update our internal statistics of the dialogue
	 * @param moves The newly played moves
	 */
	private void updateDialogueStats(List<Move<? extends Locution>> moves) {

		// Check which proposals are currently in
		Map<Proposal, Boolean> newProposalStats = new HashMap<Proposal, Boolean>();
		for (Proposal proposal : dialogue.getProposals()) {
			newProposalStats.put(proposal, proposal.isIn());
		}
		
		// Count moves and strongly relevant moves
		for (Move<? extends Locution> move : moves) {
			if (move.getLocution() instanceof DeliberationLocution) {
				DeliberationLocution loc = (DeliberationLocution) move.getLocution();
				
				// Move efficiency
				stats.e_moves++;
				
				// Relevance
				for (Proposal proposal : dialogue.getProposals()) {
					if (proposal.findNodeByIndex(move.getIndex()) != null) {
						// Changed the proposal status?
						if (!oldProposalStats.containsKey(proposal) ||
								(oldProposalStats.containsKey(proposal) && 
										oldProposalStats.get(proposal) != newProposalStats.get(proposal))) {
							relevantMoves++;
						}
						break;
					}
				}
				
				
				if (loc.getPublicBeliefs() != null) {			
					// Keep track of which beliefs were played first by which player
					for (Constant belief : loc.getPublicBeliefs()) {
						if (stats.publicbeliefs.containsKey(belief)) {
							addOneToBeliefCount(stats.otherbeliefsCount, move.getPlayer());
						} else {
							stats.publicbeliefs.put(belief, move.getPlayer());
							addOneToBeliefCount(stats.ownbeliefsCount, move.getPlayer());
						}
					}
				}
				
			}
		}
		
		// Remember the proposal statuses
		oldProposalStats = newProposalStats;
		
	}
	
	private void addOneToBeliefCount(Map<Participant, Integer> counter, Participant participant) {
		if (!counter.containsKey(participant)) {
			counter.put(participant, 1);
		} else {
			counter.put(participant, counter.get(participant) + 1);
		}
	}
	
	/**
	 * Return the results of the now-terminated dialogue to our monitor
	 */
	private void onDialogueTerminated() {

		// Agent BDI strategy properties
		// Assumes all agents use the same configuration here
		for (ParticipatingAgent participant : platform.getJoinedAgents()) {
			if (participant.getAgent() instanceof StrategyExposer) {
				stats.stratprops = ((StrategyExposer)participant.getAgent()).getStategyProperties();
				break;
			}
		}
		
		// Get agent-specific statistics
		stats.e_strongrelevance = 0F;
		if (stats.e_moves > 0) {
			stats.e_strongrelevance = (float)relevantMoves / stats.e_moves;
		}
		stats.utilities = new HashMap<Participant, Map<Constant,Integer>>();
		stats.e_privatebeliefs = new HashMap<Participant, Float>();
		stats.e_loan = new HashMap<Participant, Float>();
		stats.e_totalutility = new HashMap<Constant, Integer>();
		for (ParticipatingAgent participant : platform.getJoinedAgents()) {
			if (participant.getAgent() instanceof StrategyExposer) {
				Participant p = participant.getParticipant();
				StrategyExposer agent = (StrategyExposer) participant.getAgent();

				// Degree of loan = % of premises used that were introduced by another agent
				int ownbeliefs = (stats.ownbeliefsCount.containsKey(p)? stats.ownbeliefsCount.get(p): 0);
				int othersbeliefs = (stats.otherbeliefsCount.containsKey(p)? stats.otherbeliefsCount.get(p): 0);
				if (ownbeliefs + othersbeliefs > 0) {
					stats.e_loan.put(p, 1F - (float)ownbeliefs / (ownbeliefs + othersbeliefs));
				} else {
					stats.e_loan.put(p, null);
				}
				
				// Private belief degree
				int initialCount = agent.getInitialBeliefsCount();
				int privateCount = agent.getPrivateBeliefsCount(stats.publicbeliefs.keySet());
				if (initialCount > 0) {
					stats.e_privatebeliefs.put(p, (float)privateCount / initialCount);
				} else {
					stats.e_privatebeliefs.put(p, null);
				}

				// Utilities
				Map<Constant, Integer> agentutility = new HashMap<Constant, Integer>();
				for (ValuedOption option : agent.getAllOptions()) {
					agentutility.put(option.getOption(), option.getUtility());
					Constant setOption = getOption(stats.e_totalutility, option);
					stats.e_totalutility.put(setOption, stats.e_totalutility.get(setOption) + option.getUtility());
				}
				stats.utilities.put(p, agentutility);
				
			}
		}
		stats.e_pareto = new HashMap<Constant, Boolean>();
		/*for (Proposal proposal : dialogue.getProposals()) {
			Constant concProposal = proposal.getProposalLocution().getConcreteProposal();
			stats.e_pareto.put(concProposal, true);
			for (Proposal alternative : dialogue.getProposals()) {
				if (!proposal.equals(alternative)) {
					
					// Look at every participant...
					for (ParticipatingAgent participant : platform.getJoinedAgents()) {
						if (participant.getAgent() instanceof StrategyExposer) {
							
							// for the assigned utility to this proposal...
							List<ValuedOption> allOptions = ((StrategyExposer)participant.getAgent()).getAllOptions();
							int concProposalUtility = 0;
							for (ValuedOption option : allOptions) {
								if (option.getOption().isEqualModuloVariables(concProposal)) {
									concProposalUtility = option.getUtility();
								}
								break;
							}
							
							// and look at the utilities it assign to the alternatives...
							for (ValuedOption option : allOptions) {
								if (!option.getOption().isEqualModuloVariables(concProposal) && option.getUtility() > concProposalUtility) {
									// This alternative is preferred by the agent
									stats.e_pareto.put(concProposal, false);
									break;
								}
							}
						}
					}
					
				}
			}
		}*/
		
		// Averages and dialogue outcome
		float privatebeliefsSum = 0F;
		float loanSum = 0F;
		for (Entry<Participant, Float> p : stats.e_privatebeliefs.entrySet()) {
			privatebeliefsSum += p.getValue();
		}
		for (Entry<Participant, Float> p : stats.e_loan.entrySet()) {
			if (p.getValue() != null) { 
				loanSum += p.getValue();
			}
		}
		if (stats.o == null) {
			// No dialogue outcome; set the total outcome utility to 0
			stats.e_total_o = 0;
		}
		int totalutilitySum = 0;
		int totalutilityInSum = 0;
		stats.optionsInCount = 0;
		for (Entry<Constant, Integer> p : stats.e_totalutility.entrySet()) {
			// Total utility of all proposals
			totalutilitySum += p.getValue();
			// Normal outcome utility
			if (stats.e_total_o == null) {
				if (p.getKey().isEqualModuloVariables(stats.o)) {
					stats.e_total_o = p.getValue();
				}
			}
			// Total utility of proposals that are 'in'
			for (Proposal proposal : dialogue.getProposals()) {
				if (proposal.getProposalLocution().getConcreteProposal().isEqualModuloVariables(p.getKey()) && proposal.isIn()) {
					totalutilityInSum += p.getValue();
					stats.optionsInCount++;
				}
			}
		}
		stats.e_privatebeliefs_avg = privatebeliefsSum / stats.e_privatebeliefs.size();
		stats.e_loan_avg = loanSum / stats.e_loan.size();
		stats.e_total_avg = (float)totalutilitySum / stats.e_totalutility.size();
		stats.e_total_in_avg = stats.optionsInCount > 0? (float)totalutilityInSum / stats.optionsInCount: 0F;
		
		// Return statistics
		monitor.dialogueTerminated(stats);
		
	}

	private Constant getOption(Map<Constant, Integer> eTotalutility, ValuedOption agentOption) {
		// Find option in the list
		for (Entry<Constant, Integer> o : eTotalutility.entrySet()) {
			if (o.getKey().isEqualModuloVariables(agentOption.getOption())) {
				return o.getKey();
			}
		}
		// Doesn't exist yet: create it
		eTotalutility.put(agentOption.getOption(), 0);
		return agentOption.getOption();
	}

	// Maintain the dialogue state and see if it is terminated
	@Override
	public void onMessagesReceived(List<DialogueMessage> messages) {
		for (DialogueMessage message : messages) {
			if (dialogue != null && message instanceof DialogueStateChangeMessage) {
				
				// Update the state of our dialogue
				dialogue.setState(((DialogueStateChangeMessage)message).getNewState());
				// Terminated?
				if (dialogue.getState() == DialogueState.Terminated) {
					onDialogueTerminated();
				}

			} else if (message instanceof OutcomeMessage) {
				
				// We have a dialogue outcome now: put it in the stats
				Proposal outcome = ((OutcomeMessage)message).getOutcome();
				if (outcome == null) {
					stats.o = null;
				} else {
					stats.o = outcome.getProposalLocution().getConcreteProposal();
				}
				
			} else if (message instanceof DialogueStartedMessage) {
				
				// Dialogue started
				DialogueStartedMessage m = (DialogueStartedMessage) message;
				this.dialogue = new Dialogue(m.getTopic(), m.getTopicGoal());
				this.stats = new DialogueStats(dialogue, runId, new Date(), configId, platformSettings, experimentOptionsCount, agentStrategy);
				this.stats.e_moves = 0;
				this.stats.e_strongrelevance = 0F;
				this.stats.ownbeliefsCount = new HashMap<Participant, Integer>();
				this.stats.otherbeliefsCount = new HashMap<Participant, Integer>();
				this.stats.publicbeliefs = new HashMap<Constant, Participant>();
				// The public goal is always the first public belief
				this.stats.publicbeliefs.put(m.getTopicGoal().getGoalContent(), null);
				
			}
		}
	}

	@Override
	public void onExceptionThrown(PlatformException e) {
		// Do nothing
	}

	@Override
	public void onMoves(List<Move<? extends Locution>> moves) {
		// Update our internal dialogue model
		try {
			if (this.dialogue != null) {
				this.dialogue.update(moves);
				updateDialogueStats(moves);
			}
		} catch (DialogueException e) {
			// Invalid moves were played by some agent: ignore this
		}		
	}

}
