package nl.uu.cs.arg.platform;

import java.util.List;

import nl.uu.cs.arg.shared.dialogue.protocol.DeliberationRule;
import nl.uu.cs.arg.shared.dialogue.protocol.OutcomeSelectionRule;
import nl.uu.cs.arg.shared.dialogue.protocol.TerminationRule;

/**
 * Maintains the application constants and platform settings.
 * 
 * @author erickok
 *
 */
public class Settings {

	/*
	 * The public (human readable) application name
	 */
	public final static String APPLICATION_NAME = "Baidd";

	/*
	 * The public (human readable) application version number 
	 */
	public final static String APPLICATION_VERSION = "0.9";

	/*
	 * The public (human readable) application name with version number 
	 */
	public final static String APPLICATION_NAME_VERSION = APPLICATION_NAME + " " + APPLICATION_VERSION;

	public Settings(List<DeliberationRule> deliberationRules, List<TerminationRule> terminationRules, OutcomeSelectionRule outcomeSelectionRule) {
		this.deliberationRules = deliberationRules;
		this.terminationRules = terminationRules;
		this.outcomeSelectionRule = outcomeSelectionRule;
	}

	/**
	 * The deliberation protocol, consisting of rules that specify move validity
	 */
	private final List<DeliberationRule> deliberationRules;
	public List<DeliberationRule> getDeliberationRules() {
		return deliberationRules;
	}
	
	/**
	 * The termination rules of the deliberation protocol, specifying when a dialogue should terminate
	 */
	private final List<TerminationRule> terminationRules;
	public List<TerminationRule> getTerminationRules() {
		return terminationRules;
	}
	
	/**
	 * The outcome selection rule, which select one of the proposals in a dialogue as the winner
	 */
	private final OutcomeSelectionRule outcomeSelectionRule;
	public OutcomeSelectionRule getOutcomeSelectionRule() {
		return outcomeSelectionRule;
	}
	
}
