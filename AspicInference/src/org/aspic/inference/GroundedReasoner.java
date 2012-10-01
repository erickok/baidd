package org.aspic.inference;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;


/**
 * A Reasoner that implements Grounded Semantics.
 * 
 * @author mjs (matthew.south@cancer.org.uk)
 */
class GroundedReasoner extends AbstractReasoner {
	private static Logger logger = Logger.getLogger(GroundedReasoner.class.getName());		
		
	ReasonerPair evaluate(ReasonerPair testPair, List<RuleArgument> proof) {
		return calculate(testPair, proof, 0);
	}
	
	/**
	 *  This is more a less a direct implementation of the algorithm described in D2.6, p26
	 */ 
	ReasonerPair calculate(ReasonerPair testPair, List<RuleArgument> proof, int level) {
		int numberOfAttacks = 0;
		log("STS: " + testPair.inspect(), level);
		RuleArgument testArgument = testPair.getPRO().getArguments().get(testPair.getPRO().getArguments().size()-1); // get last element from PRO list
		testArgument.setStatus(RuleArgument.Status.DEFEATED); // assume failure
		log("OPP: starting attack against " + testArgument.getName(), level); 
		Iterator<RuleArgument> itrAttackers = testArgument.deepSuccessfulAttackingArgumentIterator(Party.OPP, level);
		boolean failure = false;
		while (itrAttackers.hasNext() && failure==false) {
			RuleArgument attacker = itrAttackers.next();
			if (!proof.contains(attacker)) proof.add(attacker);
			attacker.setStatus(RuleArgument.Status.UNDEFEATED);
			numberOfAttacks++;
			log("OPP: found " + numberWriter(numberOfAttacks) + " attack against " + testArgument.getName() + " : " + attacker.getName(), level);
			log("PRO: starting defense against " + testArgument.getName() + " <- " + attacker.getName(), level);
			// try defending against attacker
			Iterator<RuleArgument> itrDefenders = attacker.deepSuccessfulAttackingArgumentIterator(Party.PRO, level);
			boolean success = false;
			defenderLoop:
			while (itrDefenders.hasNext() && success==false) {
				RuleArgument defender = itrDefenders.next();
				if (!proof.contains(defender)) proof.add(defender);
				// ignore defenders that are already in PRO
				if (testPair.getPRO().includesSemanticallyEqual(defender)) {
					log("OPP: " + defender.getName() + " ignored because it's already used.", level);
					continue defenderLoop;
				}
				// ignore defenders that are counter-attacked (ignore loops)
				if (attacker.isDeepSuccessfulAttackerOf(defender)) continue defenderLoop;
				log("PRO: found possible defender " + testArgument.getName() + " <- " + attacker.getName() + " <- " + defender.getName(), level);
				ReasonerPair newPair = calculate(new ReasonerPair(testPair.getPRO().cloneAndExtend(defender), testPair.getOPP().cloneAndExtend(attacker)), proof, level+1);
				if (newPair!=null) {
					log("PRO: defender successful.", level);
					success=true;
					testPair = newPair;
				} else {
					log("PRO: defender failed.", level);
				}
			}
			if (success==true) {
				attacker.setStatus(RuleArgument.Status.DEFEATED);
				log("PRO: defense successful, returning new move: " + testPair.inspect(), level);
			} else {
				log("PRO: defense failed, returning empty move, {[], []}.", level);
				failure=true;
			}
		}
		if (failure==true) {
			return new ReasonerPair();
		} else {
			if (numberOfAttacks==0) {
				log("OPP: no attackers, hence attack on " + testArgument.getName() + " fails", level);
			} else {
				log("OPP: no more attackers, hence attack on " + testArgument.getName() + " fails", level);			
			}
			testArgument.setStatus(RuleArgument.Status.UNDEFEATED);
			return testPair;
		}
	}
	
	/**
	 * Write indented message to the logStream
	 * @param message The message to write
	 * @param level The level of indentation
	 */
	private void log(String message, int level) {
		// indent based on level (this is the reason level is passed around)
		StringBuffer indentedMessage = new StringBuffer();
		for (int i=0; i<level; i++) {
			indentedMessage.append("    ");
		}
		indentedMessage.append(message);
		//System.out.println("Logging fine message. current level = " + logger.getLevel().toString());
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

}
