package org.aspic.inference;

import java.util.Iterator;
import java.util.logging.Logger;
import java.util.List;


/**
 * A Reasoner that implements Preferred Credulous Semantics.
 * 
 * @author mjs (matthew.south@cancer.org.uk)
 */
class PreferredCredulousReasoner extends AbstractReasoner {
	private static Logger logger = Logger.getLogger(PreferredCredulousReasoner.class.getName());		
		
	ReasonerPair evaluate(ReasonerPair testPair, List<RuleArgument> proof) {
		return calculate(testPair, proof, 0);
	}
	
	private ReasonerPair calculate(ReasonerPair testPair, List<RuleArgument> proof, int level) {
		int numberOfAttacks = 0;
		log("STS: " + testPair.inspect(), level);
		RuleArgument testArgument = testPair.getPRO().getArguments().get(testPair.getPRO().getArguments().size()-1); // get last element from PRO list
		testArgument.setStatus(RuleArgument.Status.DEFEATED); // assume failure
		Iterator<RuleArgument> itrAttackers = testArgument.deepSuccessfulAttackingArgumentIterator(Party.OPP, level);
		log("OPP: starting attack against " + testArgument.getName(), level);
		while (itrAttackers.hasNext()) {
			try {
				RuleArgument attacker = itrAttackers.next();
				attacker.setStatus(RuleArgument.Status.UNDEFEATED);
				numberOfAttacks++;
				log("OPP: found " + numberWriter(numberOfAttacks) + " attack against " + testArgument.getName() + " : " + attacker.getName(), level);
				if (testPair.getOPP().includesSemanticallyEqual(attacker)) {
					log("OPP: protocol for admissibility forbids reuse of " + attacker.getName(), level);
					continue;
				}
				log("PRO: starting defense against " + testArgument.getName() + " <- " + attacker.getName(), level);
				// try defending with the current PRO arguments
				if (!proof.contains(attacker)) proof.add(attacker);
				Iterator<RuleArgument> itrKnownDefenders = testPair.getPRO().getArguments().iterator();
				while (itrKnownDefenders.hasNext()) {
					RuleArgument defender = itrKnownDefenders.next();
					if (defender.isSuccessfulAttackerOf(attacker)) {
						attacker.setStatus(RuleArgument.Status.DEFEATED);
						log("PRO: defense of " + testArgument.getName() + " <- " + attacker.getName() + " succeeds with " + defender.getName(), level);
						throw new SuccessfulDefenseException();
					}
				}
				// try defending with new arguments
				Iterator<RuleArgument> itrUnknownDefenders = attacker.deepSuccessfulAttackingArgumentIterator(Party.PRO, level);
				attackerLoop:
				while (itrUnknownDefenders.hasNext()) {
					RuleArgument defender = itrUnknownDefenders.next();
					log("PRO: trying defense " + testArgument.getName() + " <- " + attacker.getName() + " <- " + defender.getName(), level);
					// ignore members of PRO (because we've just looked at them)
					Iterator<RuleArgument> itrProMembers = testPair.getPRO().getArguments().iterator();
					while (itrProMembers.hasNext()) {
						RuleArgument proMember = itrProMembers.next();
						if (proMember.isSuccessfulAttackerOf(defender)) {
							log("PRO: Conflicting " + defender.getName(), level);
							continue attackerLoop;
						}
					}
					if (!proof.contains(defender)) proof.add(defender);
					// 
					ReasonerPair testPairCopy = new ReasonerPair(testPair.getPRO().cloneAndExtend(defender), testPair.getOPP().cloneAndExtend(attacker));
					if (calculate(testPairCopy, proof, level+1).getPRO().getArguments().size()!=0) {
						attacker.setStatus(RuleArgument.Status.DEFEATED);
						log("PRO: defense " + testArgument.getName() + " <- " + attacker.getName() + " <- " + defender.getName() + " succeeds", level);
						testPair = testPairCopy;
						throw new SuccessfulDefenseException();
					} else {
						log("PRO: defense " + testArgument.getName() + " <- " + attacker.toString() + " <- " + defender.toString() + " fails", level);
						log("PRO: trying next defender " + testArgument.getName() + " <- " + attacker.getName(), level);
					}
				}
				log("PRO: defense of " + testArgument.getName() + " <- " + attacker.getName() + " fails, returning null", level);
				return new ReasonerPair();
			} catch (SuccessfulDefenseException e1) {
				log("OPP: trying next attacker of " + testArgument.getName(), level);
			}
		}
		testArgument.setStatus(RuleArgument.Status.UNDEFEATED);
		if (numberOfAttacks==0) {
			log("OPP: no attackers, hence attack on " + testArgument.getName() + " fails", level);
		} else {
			log("OPP: no more attackers, hence attack on " + testArgument.getName() + " fails", level);			
		}
		log("STS: " + testArgument.getName() + " is a member of the admissible set, " + testPair.getPRO().inspect(), level);
		return testPair;
	}
	
	/**
	 * Write indented message to the logStream
	 * @param message The message to write
	 * @param level The level of indentation
	 */
	private void log(String message, int level) {
		// indent based on level (formatting of text output is the reason level is passed around)
		StringBuffer indentedMessage = new StringBuffer();
		for (int i=0; i<level; i++) {
			indentedMessage.append("   ");
		}
		indentedMessage.append(message);
		logger.fine(indentedMessage.toString());
	}
	
	/** 
	 * Return prettified index, i.e. numberWrite(97) = "97th"
	 * @param number
	 * @return prettified number
	 */
	private String numberWriter(int number) {
		switch(number) {
		case 1 : return "1st";
		case 2 : return "2nd";
		case 3 : return "3rd";
		default : return number + "th";
		}
	}
	
	private class SuccessfulDefenseException extends Exception {}
}
