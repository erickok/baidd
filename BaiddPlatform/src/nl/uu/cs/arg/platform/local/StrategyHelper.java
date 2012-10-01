package nl.uu.cs.arg.platform.local;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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
import org.aspic.inference.KnowledgeBase;
import org.aspic.inference.Query;
import org.aspic.inference.Reasoner;
import org.aspic.inference.ReasonerException;
import org.aspic.inference.Rule;
import org.aspic.inference.RuleArgument;
import org.aspic.inference.Engine.Property;
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
	public List<RuleArgument> findProof(ConstantList query, Double needed, KnowledgeBase kb, List<Rule> addKnowledge) throws ParseException, ReasonerException {
		
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
			// This is hacked by seeing if the 
			if (proof.getClaim().getFunctor().startsWith("r")) {
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
	public List<Goal> evaluateGoalSatisfaction(Constant option, List<Goal> goals, KnowledgeBase kb) throws ParseException, ReasonerException {
		
		List<Goal> satisfiedGoals = new ArrayList<Goal>();
		for (Goal goal : goals) {
			
			// A goal is satisfied by the option if we can form an argument for the 
			// goal given the belief base added with the option (if g <- B \and q)
			List<RuleArgument> proofs = findProof(new ConstantList(goal.getGoalContent()), 0.0, kb, Arrays.asList(new Rule(option)));
			if (proofs.size() > 0) {
				// This goal can be satisfied
				satisfiedGoals.add(goal);
			}
		}
		return satisfiedGoals;
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
			List<RuleArgument> proofs = findProof(new ConstantList(argumentToAttack.getClaim().negation()), argumentToAttack.getModifier(), kb, addKnowledge);
			
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
	 * @return An argument supporting the term we want to prove; or null if none could be formed
	 */
	public RuleArgument generateRebuttal(KnowledgeBase kb, Constant termToProve, double needed, Move<? extends Locution> moveToAttack, List<Move<? extends Locution>> existingReplies, List<Rule> addKnowledge) throws ParseException, ReasonerException {

		// Try to find a single argument for the term that we are trying to prove
		List<RuleArgument> proofs = findProof(new ConstantList(termToProve), needed, kb, addKnowledge);
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
		RuleArgument rebuttal = generateRebuttal(kb, argumentToAttack.getClaim().negation(), argumentToAttack.getModifier(), argueMoveToAttack, existingReplies, addKnowledge);
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
	
}