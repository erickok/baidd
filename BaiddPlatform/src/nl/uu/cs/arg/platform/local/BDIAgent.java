package nl.uu.cs.arg.platform.local;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import nl.uu.cs.arg.platform.local.ValuedOption.Strategy;
import nl.uu.cs.arg.shared.Agent;
import nl.uu.cs.arg.shared.dialogue.DialogueException;
import nl.uu.cs.arg.shared.dialogue.Goal;
import nl.uu.cs.arg.shared.dialogue.Move;
import nl.uu.cs.arg.shared.dialogue.Proposal;
import nl.uu.cs.arg.shared.dialogue.locutions.ArgueLocution;
import nl.uu.cs.arg.shared.dialogue.locutions.DeliberationLocution;
import nl.uu.cs.arg.shared.dialogue.locutions.InformLocution;
import nl.uu.cs.arg.shared.dialogue.locutions.Locution;
import nl.uu.cs.arg.shared.dialogue.locutions.ProposeLocution;
import nl.uu.cs.arg.shared.dialogue.locutions.RejectLocution;
import nl.uu.cs.arg.shared.dialogue.locutions.WhyLocution;
import nl.uu.cs.arg.shared.dialogue.locutions.WhyProposeLocution;
import nl.uu.cs.arg.shared.dialogue.locutions.WhyRejectLocution;

import org.aspic.inference.Constant;
import org.aspic.inference.ConstantList;
import org.aspic.inference.ReasonerException;
import org.aspic.inference.Rule;
import org.aspic.inference.RuleArgument;
import org.aspic.inference.Term;
import org.aspic.inference.parser.ParseException;

/**
 * The BDI agent is a fully implemented agent following the BDI 
 * architecture. It uses its beliefs and goals to generate proposals,
 * evaluate them and generate replies. Furthermore, it keeps track of
 * the dialogue and may use new beliefs that are introduced there for
 * itself.
 * 
 * The agent will always join the dialogue.
 * 
 * @author erickok
 *
 */
public class BDIAgent extends DeliberatingAgent implements Agent, StrategyExposer {

	private Map<Property, Object> properties;
	
	public enum Property {
		// Adopting beliefs
		AdoptBeliefs,
		AdoptOnlyBeliefsWithoutCounterargument,
		// Attitude assignment
		BuildMaxDestroyMin,
		BuildOnlyMaxUtility,
		BuildAllAboveAverage,
		BuildAllPositiveUtility,
		// Move generation
		PlayRejects,
		OnlyRejectIfCounterArgument,
		OnlyWhyProposeIfCounterArgument,
		OnlyWhyRejectIfArgument,
		OnlyWhyIfCounterArgument,
		SkipIfPossibe, 
		// Disable arguing
		PlayOnlyRejects,
		PlayOnlyPropose,
		PlayInformMoves
	}
	
	/**
	 * Create the agent from an XML data specification
	 * @param xmlDataFile The parsed XML data
	 */
	public BDIAgent(AgentXmlData xmlDataFile) {
		super(xmlDataFile.getName(), 
				xmlDataFile.getRawProperties(), 
				xmlDataFile.getBeliefBase(), 
				xmlDataFile.getOptions(), 
				xmlDataFile.getHiddenGoals(), 
				xmlDataFile.getPublicGoals());
		// Set property defaults
		this.properties = new HashMap<Property, Object>();
		this.properties.put(Property.AdoptBeliefs, Boolean.FALSE);
		this.properties.put(Property.AdoptOnlyBeliefsWithoutCounterargument, Boolean.TRUE);
		this.properties.put(Property.BuildMaxDestroyMin, Boolean.FALSE);
		this.properties.put(Property.BuildOnlyMaxUtility, Boolean.FALSE);
		this.properties.put(Property.BuildAllAboveAverage, Boolean.FALSE);
		this.properties.put(Property.BuildAllPositiveUtility, Boolean.TRUE);
		this.properties.put(Property.PlayRejects, Boolean.FALSE);
		this.properties.put(Property.OnlyRejectIfCounterArgument, Boolean.FALSE);
		this.properties.put(Property.OnlyWhyProposeIfCounterArgument, Boolean.FALSE);
		this.properties.put(Property.OnlyWhyRejectIfArgument, Boolean.FALSE);
		this.properties.put(Property.OnlyWhyIfCounterArgument, Boolean.TRUE);
		this.properties.put(Property.SkipIfPossibe, Boolean.FALSE);
		this.properties.put(Property.PlayOnlyPropose, Boolean.FALSE);
		this.properties.put(Property.PlayOnlyRejects, Boolean.FALSE);
		this.properties.put(Property.PlayInformMoves, Boolean.FALSE);		// For each raw property (where the key is still a String), store the internal typed property
		if (xmlDataFile.getRawProperties() != null) {
			for (Entry<String, Object> prop : xmlDataFile.getRawProperties() .entrySet()) {
				this.properties.put(Property.valueOf(Property.class, prop.getKey()), prop.getValue());
			}
		}
	}

	/**
	 * Stores new beliefs found in the newly moved locutions (both options and facts/rules)
	 * @param moves The new moves played by some agent
	 */
	@Override
	protected void storeNewBeliefs(List<Move<? extends Locution>> moves) throws ParseException, ReasonerException {

		// Store newly publicized beliefs when we have no argument against them (or an argument for them)
		for (Move<? extends Locution> move : moves) {

			if (move.getLocution() instanceof ProposeLocution) {

				// We will store any new proposal as a new option
				Constant proposal = ((ProposeLocution)move.getLocution()).getConcreteProposal();
				// Do we know about it already?
				if (!isBeliefInOptions(proposal)) {
					// Is not known yet: add it as option belief
					this.optionBeliefs.add(new Rule(proposal));
				}
				
			} else if ((Boolean)this.properties.get(Property.AdoptBeliefs) && move.getLocution() instanceof DeliberationLocution) {
				
				// Store new move beliefs
				Set<Constant> exposed = new HashSet<Constant>();
				((DeliberationLocution)move.getLocution()).gatherPublicBeliefs(exposed);
				for (Constant b : exposed) {
				
					if (b instanceof Rule) {
						// We add rules if it didn't exist yet and it doesn't cause any loops
						/*if (!beliefs.ruleExists((Rule)b) && !helper.causesLoop(beliefs, (Rule)b)) {
							beliefs.addRule((Rule)b);
						}*/
					} else {
						// We add constants and terms directly, if they are not options or the mutual goal
						if (!dialogue.getTopicGoal().getGoalContent().equals(b) && 
								!dialogue.getTopic().isUnifiable(b) && !beliefs.ruleExists(new Rule(b))) {
							if ((Boolean)this.properties.get(Property.AdoptOnlyBeliefsWithoutCounterargument)) {
								List<RuleArgument> proofs = helper.findProof(new ConstantList(b.negation()), 0.0, this.beliefs, this.optionBeliefs, null);
								if (proofs.size() == 0) {
									beliefs.addRule(new Rule(b));
								}
							} else {
								beliefs.addRule(new Rule(b));
							}
						}
					}
					
				}
				
			}
			
		}
		
	}

	/**
	 * Considering the beliefbase and a set of goals, generate new options. 
	 * This is done by querying on the dialogue topic, considering our belief base
	 * and option beliefs and see if we can form an argument. The proof of such 
	 * an argument will contain a bottom-level rule with the concrete proposal.
	 * @return A list of all the options we can think of
	 */
	@Override
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
	@Override
	protected List<ValuedOption> evaluateAllOptions(List<Constant> options) throws ParseException, ReasonerException {
		
		// For each of the known options (including existing proposals)
		List<ValuedOption> valuedOptions = new ArrayList<ValuedOption>();
		for (Constant option : options) {
			
			// Determine which goals this option satisfies
			Set<Goal> satisfied = helper.evaluateGoalSatisfaction(option, allPersonalGoals(), this.beliefs);
			
			// Sum the goal utilities to form the option utility 
			int optionUtility = 0;
			for (Goal goal : satisfied) {
				// NOTE: We don't consider goals that we have no utility value for
				if (goal instanceof ValuedGoal) {
					optionUtility += ((ValuedGoal)goal).getUtility();
				}
			}
			valuedOptions.add(new ValuedOption(option, optionUtility));
			
		}
		return valuedOptions;
		
	}

	@Override
	protected void analyseOptions(List<ValuedOption> valuedOptions) {
		
		if ((Boolean)this.properties.get(Property.BuildMaxDestroyMin)) {

			ValuedOption maxUtility = null, minUtility = null;
			for (ValuedOption option : valuedOptions) {
				// By default we are indifferent
				option.updateStrategy(Strategy.Indifferent);
				// Store which has the highest and lowest utility
				if (maxUtility == null || maxUtility.getUtility() < option.getUtility()) {
					maxUtility = option;
				}
				if (minUtility == null || minUtility.getUtility() > option.getUtility()) {
					minUtility = option;
				}
			}
			
			// Assign a build strategy to the option with the highest and lowest utility
			if (maxUtility != null && maxUtility.getUtility() > 0) {
				maxUtility.updateStrategy(Strategy.Build);
			}
			if (minUtility != null && minUtility != maxUtility) {
				minUtility.updateStrategy(Strategy.Destroy);
			}
			
		} else if ((Boolean)this.properties.get(Property.BuildOnlyMaxUtility)) {

			ValuedOption maxUtility = null;
			for (ValuedOption option : valuedOptions) {
				// By default we destroy
				option.updateStrategy(Strategy.Destroy);
				// Store which has the highest utility
				if (maxUtility == null || maxUtility.getUtility() < option.getUtility()) {
					maxUtility = option;
				}
			}
			
			// Assign a build strategy to the option with the highest utility
			if (maxUtility != null && maxUtility.getUtility() > 0) {
				maxUtility.updateStrategy(Strategy.Build);
			}

		} else if ((Boolean)this.properties.get(Property.BuildAllAboveAverage)) {
			
			int total = 0;
			for (ValuedOption option : valuedOptions) {
				total += option.getUtility();
			}
			final float average = (float)total / (float)valuedOptions.size();
			for (ValuedOption option : valuedOptions) {
				// Build if we have a utility at least as high as the average (and higher than 0)
				option.updateStrategy(option.getUtility() > 0 && option.getUtility() >= average ? 
						Strategy.Build: Strategy.Destroy);
			}

		} else if ((Boolean)this.properties.get(Property.BuildAllPositiveUtility)) {
			
			for (ValuedOption option : valuedOptions) {
				// Build if we have a positive utility
				option.updateStrategy(option.getUtility() > 0 ? Strategy.Build: Strategy.Destroy);
			}

		}
		
	}

	@SuppressWarnings("unchecked")
	@Override
	protected List<Move<? extends Locution>> generateMoves(List<ValuedOption> valuedOptions) throws DialogueException, ParseException, ReasonerException {
		List<Move<? extends Locution>> moves = new ArrayList<Move<? extends Locution>>();		
		for (ValuedOption option : valuedOptions) {
			
			// See if it already exists in the dialogue as proposal
			Proposal existingProposal = null;
			for (Proposal proposed : this.dialogue.getProposals()) {
				if (proposed.getProposalLocution().getConcreteProposal().equals(option.getOption())) {
					existingProposal = proposed;
					break;
				}
			}

			if (existingProposal == null) {
				
				// Make a new proposal for the options with build strategy that were not yet proposed
				if (option.getStrategy() == Strategy.Build) {
					moves.add(Move.buildMove(this.participant, null, new ProposeLocution(option.getOption())));
				}
				continue;
			
			}
			
			// Already proposed?
			boolean existingIsIn = existingProposal.isIn();
			// Should be attack?
			if (option.getStrategy() == Strategy.Build && existingIsIn || 
					option.getStrategy() == Strategy.Destroy && !existingIsIn) {
				continue;
			}
			
			// Generate attackers in the dialogue

			// Exceptional cases that we do not argue, but only propose and possible reject
			if ((Boolean)this.properties.get(Property.PlayOnlyPropose)) {
				continue;
			}
			if ((Boolean)this.properties.get(Property.PlayOnlyRejects)) {

				// Does it already have a reject reply?
				boolean hasReject = false;
				for (Move<? extends Locution> reply : existingProposal.getReplies(existingProposal.getProposalMove())) {
					if (reply.getLocution() instanceof RejectLocution) {
						hasReject = true;
						break;
					}
				}
				if (!hasReject) {
					moves.add(Move.buildMove(this.participant, existingProposal.getProposalMove(), new RejectLocution(existingProposal)));
				}
				continue;
				
			}
			
			// Find a place to attack/support this proposal
			// TODO?: Sort list on 'easy' attacks first and 'hard' attacks later? Or randomize list?
			List<Move<? extends Locution>> attackers = existingProposal.getActiveAttackers();
			for (Move<? extends Locution> attackMove : attackers) {

				// Some shortcuts to access often-used values
				Locution attacker = attackMove.getLocution();
				List<Move<? extends Locution>> replies = existingProposal.getReplies(attackMove);
				Constant topicGoal = dialogue.getTopicGoal().getGoalContent();
				List<Rule> optionAsKnowledge = Arrays.asList(new Rule(existingProposal.getProposalLocution().getConcreteProposal()));
				Constant requireOptionPremise = existingProposal.getProposalLocution().getConcreteProposal();

				// Try to generate a counter-argument first
				RuleArgument newArgue = null;
				if (attacker instanceof WhyProposeLocution) {
					// Find argument to support a goal given this proposal
					newArgue = helper.generateArgument(this.beliefs, topicGoal, 0.0, attackMove, replies, 
							optionAsKnowledge, requireOptionPremise);
				} else if (attacker instanceof WhyRejectLocution) {
					// Find argument for the negation of a goal given this proposal
					newArgue = helper.generateArgument(this.beliefs, topicGoal.negation(), 0.0, attackMove, replies, 
							optionAsKnowledge, requireOptionPremise);
				} else if (attacker instanceof WhyLocution) {
					// Find argument to support a premise that was questioned in a why move
					newArgue = helper.generateArgument(this.beliefs, ((WhyLocution)attacker).getAttackedPremise(), 
							0.0, attackMove, replies);
				} else if (attacker instanceof ArgueLocution) {
					newArgue = helper.generateCounterAttack(this.beliefs, ((ArgueLocution)attacker).getArgument(), 
							(Move<ArgueLocution>) attackMove, replies, optionAsKnowledge);
				}
				// Found an argument? Then make the move
				if (newArgue != null) {
					moves.add(Move.buildMove(this.participant, attackMove, new ArgueLocution(newArgue)));
					break;
				}

				// We don't have a counter-argument
				// Try to question the move
				if (attacker instanceof ProposeLocution && existingIsIn) {
					
					// A propose move may be questioned with why-propose or reject.
					// Does it already have a why-propose or reject reply?
					boolean hasWhy = false, hasReject = false;
					for (Move<? extends Locution> reply : replies) {
						if (reply.getLocution() instanceof WhyProposeLocution) {
							hasWhy = true;
						} else if (reply.getLocution() instanceof RejectLocution) {
							hasReject = true;
						}
						if (hasWhy && hasReject) {
							break;
						}
					}
					if (!hasWhy) {
						// No why-propose reply yet: check if we can (should) make this move
						if (!((Boolean) this.properties.get(Property.OnlyWhyProposeIfCounterArgument)) ||
								helper.generateArgument(this.beliefs, dialogue.getTopicGoal().getGoalContent(), 0.0, 
										attackMove, replies, optionAsKnowledge, requireOptionPremise) != null) {
							moves.add(Move.buildMove(this.participant, attackMove, new WhyProposeLocution(existingProposal)));
							break;
						}
					} else if (!hasReject && (Boolean) this.properties.get(Property.PlayRejects)) {
						// No reject reply yet: check if we can (should) make this move
						if (!((Boolean) this.properties.get(Property.OnlyRejectIfCounterArgument)) ||
								helper.generateArgument(this.beliefs, dialogue.getTopicGoal().getGoalContent().negation(), 0.0, 
										attackMove, replies, optionAsKnowledge, requireOptionPremise) != null) {
							moves.add(Move.buildMove(this.participant, attackMove, new RejectLocution(existingProposal)));
							break;
						}
					}

				} else if (attacker instanceof RejectLocution) {
					
					// A reject move may be questioned with a why-reject.
					// Does it already have a why-reject reply?
					boolean hasWhy = false;
					for (Move<? extends Locution> reply : replies) {
						if (reply.getLocution() instanceof WhyRejectLocution) {
							hasWhy = true;
							break;
						}
					}
					if (!hasWhy) {
						// No why-reject reply yet: check if we can (should) make this move
						if (!((Boolean) this.properties.get(Property.OnlyWhyRejectIfArgument)) ||
								helper.generateArgument(this.beliefs, this.dialogue.getTopicGoal().getGoalContent(), 
										0.0, attackMove, replies, optionAsKnowledge) == null) {
							moves.add(Move.buildMove(this.participant, attackMove, new WhyRejectLocution(existingProposal)));
							break;
						}
					}
					
				} else if (attacker instanceof ArgueLocution) {
					
					// An argue(A) move may be questioned with why(q) where q \in prem(A): check if we can (should) make this move
					WhyLocution whyLocution = null;
					if ((Boolean) this.properties.get(Property.OnlyWhyIfCounterArgument)) {
						// Check if we actually have a counter argument
						RuleArgument underminer = helper.generateUnderminerOrUndercutter(this.beliefs, ((ArgueLocution)attacker).getArgument(), 
								(Move<ArgueLocution>) attackMove, replies, optionAsKnowledge);
						if (underminer != null) {
							whyLocution = new WhyLocution(underminer.getClaim().negation());
						}
					} else {
						// No checking: just generate a why move for the first non-questioned premise
						Constant premiseToAttack = helper.generateUncheckedUnderminerOrUndercutter(((ArgueLocution)attacker).getArgument(), 
								existingProposal, (Move<ArgueLocution>) attackMove, replies, 
								existingProposal.getProposalLocution().getConcreteProposal());
						if (premiseToAttack != null) {
							whyLocution = new WhyLocution(premiseToAttack);
						}
					}
					
					if (whyLocution != null) {
						moves.add(Move.buildMove(this.participant, attackMove, whyLocution));
						break;
					}
					
				}

			}
			
		}
		
		if ((Boolean)this.properties.get(Property.PlayInformMoves)) {
			
			// See if some of the agent's beliefs have not yet been exposed
			for (Rule rule : this.beliefs.getRules()) {
				if (!rule.isAutoGenerated() && rule.isFact()) {
					Constant belief = rule;
					if (!this.dialogue.getInformedBeliefs().contains(belief)) {
						// This belief was not yet played, so make an inform move now 
						moves.add(Move.buildMove(this.participant, null, new InformLocution(belief)));
					}
				}
			}
			
		}
		return moves;
	}
	
	/**
	 * Returns a list of all personal goals, both hidden and public. 
	 * It does not necessarily contain the dialogue goal.
	 * @return A list of all personal {@link Goal}s, which might be {@link ValuedGoal}s
	 */
	private List<Goal> allPersonalGoals() {
		List<Goal> allGoals = new ArrayList<Goal>(this.goalsHidden);
		allGoals.addAll(this.goalsPublic);
		return allGoals;
	}

	/**
	 * Returns the strategy configuration, e.g. the property settings
	 */
	@Override
	public Map<String, Object> getStategyProperties() {
		Map<String, Object> props = new HashMap<String, Object>();
		for (Entry<Property, Object> prop : properties.entrySet()) {
			props.put(prop.getKey().name(), prop.getValue());
		}
		return props;
	}

}
