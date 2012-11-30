package nl.uu.cs.arg.platform.local;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import nl.uu.cs.arg.shared.dialogue.DialogueException;
import nl.uu.cs.arg.shared.dialogue.Goal;
import nl.uu.cs.arg.shared.dialogue.Move;
import nl.uu.cs.arg.shared.dialogue.Proposal;
import nl.uu.cs.arg.shared.dialogue.locutions.ArgueLocution;
import nl.uu.cs.arg.shared.dialogue.locutions.Locution;
import nl.uu.cs.arg.shared.dialogue.locutions.WhyLocution;

import org.aspic.inference.Constant;
import org.aspic.inference.ConstantList;
import org.aspic.inference.Engine;
import org.aspic.inference.Engine.Property;
import org.aspic.inference.Element;
import org.aspic.inference.KnowledgeBase;
import org.aspic.inference.Query;
import org.aspic.inference.Reasoner;
import org.aspic.inference.ReasonerException;
import org.aspic.inference.Rule;
import org.aspic.inference.RuleArgument;
import org.aspic.inference.parser.ParseException;

public class StrategyHelper {
	
	public static StrategyHelper DefaultHelper = new StrategyHelper(Reasoner.PREFERRED_CREDULOUS);
	
	private Reasoner reasonerToUse;

	public StrategyHelper(Reasoner reasonerToUse) {
		this.reasonerToUse = reasonerToUse;
	}

	public Reasoner getReasonerToUse() {
		return reasonerToUse;
	}

	public void setReasonerToUse(Reasoner reasonerToUse) {
		this.reasonerToUse = reasonerToUse;
	}

	/**
	 * Run a single query on some knowledge base and find all the proofs for it. This
	 * uses the ASPIC logic reasoner. It may add specific knowledge for this query
	 * before running it. This is used to see if you can still infer some query when
	 * the extra knowledge is considered, e.g. if some goal can be inferred considering
	 * some proposal.
	 * @param query The term to find proof for in our belief base, e.g. a personal or mutual goal
	 * @param kb A reference to knowledge base to query against
	 * @param addKnowledge A set of rules to add to the knowledge base first (which won't be added to the kb permanently)
	 * @param needed The minimum required degree of belief (support)
	 * @return A list of proofs found for the query; these may or may not be defeated 
	 */
	public List<RuleArgument> findProof(ConstantList query, Double needed, KnowledgeBase kb, List<Rule> addKnowledge, Constant requiredPremise) throws ParseException, ReasonerException {
		
		//KnowledgeBase useKb = (KnowledgeBase) kb.clone(); // KnowledgeBase.clone() leaks memory!
		KnowledgeBase useKb = kb;
		// Add new knowledge
		if (addKnowledge != null) {
			useKb.addRules(addKnowledge);
		} 
		
		// Start the reasoning engine on our query
		Engine engine = new Engine(useKb);
		engine.setProperty(Property.SEMANTICS, this.getReasonerToUse());
		Query runQuery = engine.createQuery(query);
		List<RuleArgument> proofs = new LinkedList<RuleArgument>();
		for (RuleArgument proof: runQuery.getProof()) {
			// Throw away trivial undercutter counter-arguments: these are not allowed by ASPIC but the implementation does return them
			// This is hacked by seeing if the claim isn't a rule name (i.e. starting with 'r')
			if (proof.getClaim().getFunctor().startsWith("r")) {
				continue;
			}
			// If some constant is required to be present as premise in the argument, test if this is the case
			if (requiredPremise != null && !onBasisOfConstant(requiredPremise, proof)) {
				continue;
			}
			// Test the argument strength
			if (proof.getModifier() >= needed) {
				proofs.add(proof);
			}
		}
		
		// Remove added knowledge
		if (addKnowledge != null)
			for (Rule r : addKnowledge) { kb.removeRule(r); }
		
		return proofs;
		
	}

	/**
	 * Determines which goals are satisfied by the supplied option
	 * @param option The option to check goal satisfaction for
	 * @param goals All the goals to consider
	 * @param kb A reference to the knowledge base to query against
	 * @return The list of goals that are satisfied 
	 */
	public Set<Goal> evaluateGoalSatisfaction(Constant option, List<Goal> goals, KnowledgeBase kb) throws ParseException, ReasonerException {
		
		Set<Goal> satisfiedGoals = new HashSet<Goal>();
		for (Goal goal : goals) {
			
			// A goal is satisfied by the option if we can form an argument for the 
			// goal given the belief base added with the option
			// We require the option to be used as premise in this argument
			List<RuleArgument> proofs = findProof(new ConstantList(goal.getGoalContent()), 0.0, kb, Arrays.asList(new Rule(option)), option);
			if (proofs.size() > 0) {
				satisfiedGoals.add(goal);
			}
		}
		return satisfiedGoals;
	}

	private boolean onBasisOfConstant(Constant p, RuleArgument arg) {
		if (arg.getClaim().isEqualModuloVariables(p)) {
			return true;
		}
		for (RuleArgument sub : arg.getSubArgumentList().getArguments()) {
			if (onBasisOfConstant(p, sub)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Tries to find a valid underminer of undercutter of some argue move that 
	 * we want to attack. Returns null if none could be found.
	 * @param kb A reference to the knowledge base to query against
	 * @param argumentToAttack The argument that we are generating the underminer or undercutter for (this may be different than the argueMoveToAttack's argument when looking into its sub-arguments)
	 * @param argueMoveToAttack The argue move that we want to attack
	 * @param existingReplies The existing replies to the argue move that we want to attack
	 * @return A list of all the proposals that are viable, i.e. that we can build a sufficient argument for
	 * @return A single argument that undermines or undercuts the given argue move
	 */
	public RuleArgument generateUnderminerOrUndercutter(KnowledgeBase kb, RuleArgument argumentToAttack, Move<ArgueLocution> argueMoveToAttack, List<Move<? extends Locution>> existingReplies, List<Rule> addKnowledge) throws ParseException, ReasonerException {

		// Premises are atomic claims
		//if (argumentToAttack.isAtomic()) {
			
			// Find arguments for the negation of this claim
			List<RuleArgument> proofs = findProof(new ConstantList(argumentToAttack.getClaim().negation()), argumentToAttack.getModifier(), kb, addKnowledge, null);
			
			// If an argument can be formed that was not yet moved, return this as the new underminer
			RuleArgument newArgument = null;
			for (RuleArgument proof : proofs) {
				boolean alreadyUsed = false;
				
				// Look if we didn't already move it earlier in the branch
				Move<? extends Locution> target = argueMoveToAttack.getTarget();
				while (target != null) {
					if (target.getLocution() instanceof ArgueLocution && ((ArgueLocution)target.getLocution()).getArgument().isSemanticallyEqual(proof)) {
						// This existing argue move has the same claim as the new found prove
						alreadyUsed = true;
						break;
					} else if (target.getLocution() instanceof WhyLocution && ((WhyLocution)target.getLocution()).getAttackedPremise().isEqualModuloVariables(proof.getClaim().negation())) {
						// This existing why move already questions the claim of the new found proof
						alreadyUsed = true;
						break;
					}
					target = target.getTarget();
				}
				
				if (!alreadyUsed) {
					// Look if we already moved it as reply to this argue move that we are attacking now
					for (Move<? extends Locution> existingReply : existingReplies) {
						if (existingReply.getLocution() instanceof ArgueLocution && ((ArgueLocution)existingReply.getLocution()).getArgument().isSemanticallyEqual(proof)) {
							// This existing argue move has the same claim as the new found prove
							alreadyUsed = true;
							break;
						} else if (existingReply.getLocution() instanceof WhyLocution && ((WhyLocution)existingReply.getLocution()).getAttackedPremise().isEqualModuloVariables(proof.getClaim().negation())) {
							// This existing why move already questions the claim of the new found proof
							alreadyUsed = true;
							break;
						}
					}
				}
				
				if (!alreadyUsed) {
					newArgument = proof;
					break;
				}
			}
			if (newArgument != null) {
				return newArgument;
			}
			
		//}
		
		// Try to find a single argument that attacks one of the premises used in the argumentToAttack
		for (RuleArgument subArgument : argumentToAttack.getSubArgumentList().getArguments()) {
			RuleArgument newFound = generateUnderminerOrUndercutter(kb, subArgument, argueMoveToAttack, existingReplies, addKnowledge);
			if (newFound != null) {
				return newFound;
			}
		}
		
		// No underminer/undercutter found at all for this argument or any of its subarguments
		return null;
		
	}

	public RuleArgument generateArgument(KnowledgeBase kb, Constant termToProve, double needed, Move<? extends Locution> moveToAttack, List<Move<? extends Locution>> existingReplies) throws ParseException, ReasonerException {
		return generateArgument(kb, termToProve, needed, moveToAttack, existingReplies, null);
	}
	
	public RuleArgument generateArgument(KnowledgeBase kb, Constant termToProve, double needed, Move<? extends Locution> moveToAttack, List<Move<? extends Locution>> existingReplies, List<Rule> addKnowledge) throws ParseException, ReasonerException {
		return generateArgument(kb, termToProve, needed, moveToAttack, existingReplies, addKnowledge, null);
	}
	
	/**
	 * Tries to generate an argument for some term; this can be used to find support 
	 * for why-propose and why moves. Alternatively this may be called with a 
	 * term's negation to find proof that can be used to attack some term, like in
	 * reply to a why-reject move.
	 * @param kb A reference to the knowledge base to query against
	 * @param termToProve The term that we want to support
	 * @param needed The minimum required degree of belief (support)
	 * @param moveToAttack The move that we want to attack
	 * @param existingReplies The existing replies to the argue move that we want to attack
	 * @param addKnowledge Some beliefs to temporarily add to the knowledge base, just for this query
	 * @param requiredPremise Optionally a premise that is mandatory to be used as premise in any found argument
	 * @return An argument supporting the term we want to prove; or null if none could be formed
	 */
	public RuleArgument generateArgument(KnowledgeBase kb, Constant termToProve, double needed, Move<? extends Locution> moveToAttack, List<Move<? extends Locution>> existingReplies, List<Rule> addKnowledge, Constant requiredPremise) throws ParseException, ReasonerException {

		// Try to find a single argument for the term that we are trying to prove
		List<RuleArgument> proofs = findProof(new ConstantList(termToProve), needed, kb, addKnowledge, requiredPremise);
		for (RuleArgument proof : proofs) {

			// Look if we didn't already move it earlier in the branch
			boolean alreadyUsed = false;
			Move<? extends Locution> target = moveToAttack.getTarget();
			while (target != null) {
				if (target.getLocution() instanceof ArgueLocution && ((ArgueLocution)target.getLocution()).getArgument().isSemanticallyEqual(proof)) {
					alreadyUsed = true;
					break;
				} else if (target.getLocution() instanceof WhyLocution && ((WhyLocution)target.getLocution()).getAttackedPremise().isEqualModuloVariables(proof.getClaim().negation())) {
					alreadyUsed = true;
					break;
				}
				target = target.getTarget();
			}

			if (!alreadyUsed) {
				// Look if we already moved it as reply to this argue move that we are attacking now
				for (Move<? extends Locution> existingReply : existingReplies) {
					if (existingReply.getLocution() instanceof ArgueLocution && ((ArgueLocution)existingReply.getLocution()).getArgument().isSemanticallyEqual(proof)) {
						// This existing argue move contains the (semantically) same argument as the new found proof
						alreadyUsed = true;
						break;
					}
				}
			}
			
			if (!alreadyUsed) {
				return proof;
			}
			
		}
		
		// No argument can be formed to support this term (that wasn't already used in this proposal branch)
		return null;
		
	}
	
	public RuleArgument generateCounterAttack(KnowledgeBase kb, RuleArgument argumentToAttack, Move<ArgueLocution> argueMoveToAttack, List<Move<? extends Locution>> existingReplies, List<Rule> addKnowledge) throws ParseException, ReasonerException {
		
		// Try to attack the move's conclusion (rebutting)
		RuleArgument rebuttal = generateArgument(kb, argumentToAttack.getClaim().negation(), argumentToAttack.getModifier(), argueMoveToAttack, existingReplies, addKnowledge);
		if (rebuttal != null) {
			return rebuttal;
		}
		
		// Try to attack a premise (undermining) or used rule (undercutting) of the move's argument
		RuleArgument underminer = generateUnderminerOrUndercutter(kb, argumentToAttack, argueMoveToAttack, existingReplies, addKnowledge);
		if (underminer != null) {
			return underminer;
		}
		
		// No counter-argument can be formed
		return null;
		
	}

	public Constant generateUncheckedUnderminerOrUndercutter(RuleArgument argumentToAttack, Proposal proposal, Move<ArgueLocution> argueMoveToAttack, List<Move<? extends Locution>> existingReplies, Constant dialogueTopic) throws DialogueException {

		// Premises are atomic claims and we don't consider the dialogue topic as a premise to attack
		if (argumentToAttack.isAtomic() && !argumentToAttack.getClaim().equals(dialogueTopic)) {
			
			// Look if we didn't already attack it earlier in the branch
			boolean alreadyUsed = false;
			Move<? extends Locution> target = argueMoveToAttack;
			while (target != null) {
				if (target.getLocution() instanceof ArgueLocution) {
					for (Move<? extends Locution> reply : proposal.getReplies(target)) {
						if (reply.getLocution() instanceof WhyLocution && ((WhyLocution)reply.getLocution()).getAttackedPremise().equals(argumentToAttack.getClaim())) {
							alreadyUsed = true;
							break;
						}
					}
					
				}
				target = target.getTarget();
			}
						
			if (!alreadyUsed) {
				return argumentToAttack.getClaim();
			}
			
		}
		
		// Try to find a single argument that attacks one of the premises used in the argumentToAttack
		for (RuleArgument subArgument : argumentToAttack.getSubArgumentList().getArguments()) {
			Constant newFound = generateUncheckedUnderminerOrUndercutter(subArgument, proposal, argueMoveToAttack, existingReplies, dialogueTopic);
			if (newFound != null) {
				return newFound;
			}
		}
		
		// No underminer/undercutter found at all for this argument or any of its subarguments
		return null;
		
	}

	/**
	 * Look whether a new rule would cause a loop when applying rules (which is a way of circular reasoning not
	 * supported by the AspicInference project)
	 * @param beliefs The current rule pool
	 * @param newRule The new rule we are looking to add
	 * @return True if the new rule would cause a loop, false otherwise
	 */
	public boolean causesLoop(KnowledgeBase beliefs, Rule newRule) {
		List<Rule> applied = new ArrayList<Rule>();
		List<Rule> newRules = beliefs.getRules();
		newRules.add(newRule);
		applied.add(newRule);
		return causesLoop(newRules, applied, newRule);
	}

	// Used internally to recursively look for loops in the rules
	private boolean causesLoop(List<Rule> allRules, List<Rule> applied, Rule testRule) {

		// For each antecedent
		for (Element a : testRule.getAntecedent()) {
			// Check if there is a rule that can be applied
			for (Rule r : allRules) {
				if (r.getConsequent().isUnifiable(a)) {

					if (applied.contains(r)) {
						// If we already applied this rule earlier, we have a loop!
						return true;
					} else {
						// Not applied yet: add it to applied and look it it can cause a loop itself
						applied.add(r);
						if (causesLoop(allRules, applied, r)) {
							return true;
							// If not, continue looking
						}
					}

				}
			}
		}
		// None of the rule that we needed to apply caused a loop
		return false;
	}

}