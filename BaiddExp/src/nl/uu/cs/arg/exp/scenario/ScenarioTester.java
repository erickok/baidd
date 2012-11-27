package nl.uu.cs.arg.exp.scenario;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import nl.uu.cs.arg.platform.local.StrategyHelper;
import nl.uu.cs.arg.shared.scenario.ScenarioGenerator;
import nl.uu.cs.arg.shared.scenario.ScenarioGenerator.AssignmentMethod;
import nl.uu.cs.arg.shared.scenario.ScenarioGenerator.ConflictMethod;
import nl.uu.cs.arg.shared.scenario.ScenarioGenerator.InvalidConfigurationException;
import nl.uu.cs.arg.shared.scenario.ScenarioGenerator.Pool;

import org.aspic.inference.Constant;
import org.aspic.inference.ConstantList;
import org.aspic.inference.KnowledgeBase;
import org.aspic.inference.ReasonerException;
import org.aspic.inference.Rule;
import org.aspic.inference.RuleArgument;
import org.aspic.inference.Term;
import org.aspic.inference.parser.ParseException;

public class ScenarioTester {

	public interface ScenarioTestListener {
		public void writeHeader(String confDiffHeader);
		public void writeResult(String configuration, String confDiff, int totalA, int totalO, int totalArgToGd,
				int totalCounterArgToGd, int totalArg, int totalCounterArg);
		public void writeError(String string);
		public void finalise();
	}

	private final String g_d;
	private final int n_A[];
	private final int n_R[];
	private final int n_B_s[];
	private final int n_O_s[];
	private final int n_G_s[];
	private final int n_O_r[];
	private final int n_G_r[];
	private final int l[];
	private final int n_G_nro[];
	private final int n_B_ra[];
	private final int n_B_nra[];
	private final AssignmentMethod beliefAssignment;
	private final AssignmentMethod roleAssignment;
	private final ConflictMethod[] conflictMethods;
	private final boolean needsToNotHaveAnArgument;
	private final StrategyHelper helper = StrategyHelper.DefaultHelper;
	private final List<ScenarioTestListener> allTestListeners = new ArrayList<ScenarioTestListener>();
	private final boolean printResultsToConsole;
	
	private static final Random random = new Random();

	public ScenarioTester(String g_d, boolean printResultsToConsole) {
		this.g_d = g_d;
		this.n_A = new int[] {};
		this.n_R = new int[] {};
		this.n_B_s = new int[] {};
		this.n_O_s = new int[] {};
		this.n_G_s = new int[] {};
		this.n_O_r = new int[] {};
		this.n_G_r = new int[] {};
		this.l = new int[] {};
		this.n_G_nro = new int[] {};
		this.n_B_ra = new int[] {};
		this.n_B_nra = new int[] {};
		this.beliefAssignment = null;
		this.roleAssignment = null;
		this.conflictMethods = null;
		this.needsToNotHaveAnArgument = false;
		this.printResultsToConsole = printResultsToConsole;
	}

	public ScenarioTester(String g_d, int[] n_A, int[] n_R, int[] n_B_c, int[] n_O_c, int[] n_G_c, int[] n_O_r,
			int[] n_G_r, int[] l, int[] n_G_nro, int[] n_B_ra, int[] n_B_nra,
			AssignmentMethod beliefAssignment, AssignmentMethod roleAssignment, ConflictMethod[] conflictMethods,
			boolean needsToNotHaveAnArgument, boolean printResultsToConsole) {
		this.g_d = g_d;
		this.n_A = n_A;
		this.n_R = n_R;
		this.n_B_s = n_B_c;
		this.n_O_s = n_O_c;
		this.n_G_s = n_G_c;
		this.n_O_r = n_O_r;
		this.n_G_r = n_G_r;
		this.l = l;
		this.n_G_nro = n_G_nro;
		this.n_B_ra = n_B_ra;
		this.n_B_nra = n_B_nra;
		this.beliefAssignment = beliefAssignment;
		this.roleAssignment = roleAssignment;
		this.conflictMethods = conflictMethods;
		this.needsToNotHaveAnArgument = needsToNotHaveAnArgument;
		this.printResultsToConsole = printResultsToConsole;
	}

	public void addListener(ScenarioTestListener listener) {
		allTestListeners.add(listener);
	}
	
	/**
	 * Main entry point to test the generation of scenarios
	 * 
	 * @param args Command line arguments
	 */
	public static void main(String[] args) {
		ScenarioTester tester = new ScenarioTester("g_d", 
				new int[] { 4 }, // n_A
				new int[] { 4 }, // n_R
				new int[] { 100 }, // n_B_s
				new int[] { 13 }, // n_O_s
				new int[] { 9 }, // n_G_s
				new int[] { 2 }, // n_O_r
				new int[] { 5 }, // n_G_r
				new int[] { 3 }, // l
				new int[] { 2 }, // n_G_nro
				new int[] { 75 }, // n_B_ra
				new int[] { 6 }, // n_B_nra
				AssignmentMethod.Randomly, AssignmentMethod.Evenly, new ConflictMethod[] { ConflictMethod.Chained },
				false, true);
		try {
			tester.testRandomSetting();
		} catch (InvalidConfigurationException e) {
			// Ignore; already printed this error
		}

	}

	public void testRandomSetting() throws InvalidConfigurationException {
		
		// Pick a random setting for each of the variables
		int i_A = random.nextInt(n_A.length);
		int i_R = random.nextInt(n_R.length);
		int i_B_s = random.nextInt(n_B_s.length);
		int i_O_s = random.nextInt(n_O_s.length);
		int i_G_s = random.nextInt(n_G_s.length);
		int i_O_r = random.nextInt(n_O_r.length);
		int i_G_r = random.nextInt(n_G_r.length);
		int i_l = random.nextInt(l.length);
		int i_G_nro = random.nextInt(n_G_nro.length);
		int i_B_ra = random.nextInt(n_B_ra.length);
		int i_B_nra = random.nextInt(n_B_nra.length);
		int i_conflictMethod = random.nextInt(conflictMethods.length);

		// Construct a header and the configuration for the variables that actually differ (i.e. are experimented with)
		String confDiffHeader = "";
		String confDiff = "";
		if (conflictMethods.length > 1) {
			confDiffHeader += ScenarioExperiment.COLUMN_SEPARATOR + "Conflicts";
			confDiff += ScenarioExperiment.COLUMN_SEPARATOR + conflictMethods[i_conflictMethod];
		}
		if (n_A.length > 1) {
			confDiffHeader += ScenarioExperiment.COLUMN_SEPARATOR + "A";
			confDiff += ScenarioExperiment.COLUMN_SEPARATOR + n_A[i_A];
		}
		if (n_R.length > 1) {
			confDiffHeader += ScenarioExperiment.COLUMN_SEPARATOR + "R";
			confDiff += ScenarioExperiment.COLUMN_SEPARATOR + n_R[i_R];
		}
		if (n_B_s.length > 1) {
			confDiffHeader += ScenarioExperiment.COLUMN_SEPARATOR + "B_s";
			confDiff += ScenarioExperiment.COLUMN_SEPARATOR + n_B_s[i_B_s];
		}
		if (n_O_s.length > 1) {
			confDiffHeader += ScenarioExperiment.COLUMN_SEPARATOR + "O_s";
			confDiff += ScenarioExperiment.COLUMN_SEPARATOR + n_O_s[i_O_s];
		}
		if (n_G_s.length > 1) {
			confDiffHeader += ScenarioExperiment.COLUMN_SEPARATOR + "G_s";
			confDiff += ScenarioExperiment.COLUMN_SEPARATOR + n_G_s[i_G_s];
		}
		if (n_O_r.length > 1) {
			confDiffHeader += ScenarioExperiment.COLUMN_SEPARATOR + "O_r";
			confDiff += ScenarioExperiment.COLUMN_SEPARATOR + n_O_r[i_O_r];
		}
		if (n_G_r.length > 1) {
			confDiffHeader += ScenarioExperiment.COLUMN_SEPARATOR + "G_r";
			confDiff += ScenarioExperiment.COLUMN_SEPARATOR + n_G_r[i_G_r];
		}
		if (l.length > 1) {
			confDiffHeader += ScenarioExperiment.COLUMN_SEPARATOR + "l";
			confDiff += ScenarioExperiment.COLUMN_SEPARATOR + l[i_l];
		}
		if (n_G_nro.length > 1) {
			confDiffHeader += ScenarioExperiment.COLUMN_SEPARATOR + "G_nro";
			confDiff += ScenarioExperiment.COLUMN_SEPARATOR + n_G_nro[i_G_nro];
		}
		if (n_B_ra.length > 1) {
			confDiffHeader += ScenarioExperiment.COLUMN_SEPARATOR + "B_ra";
			confDiff += ScenarioExperiment.COLUMN_SEPARATOR + n_B_ra[i_B_ra];
		}
		if (n_B_nra.length > 1) {
			confDiffHeader += ScenarioExperiment.COLUMN_SEPARATOR + "B_nra";
			confDiff += ScenarioExperiment.COLUMN_SEPARATOR + n_B_nra[i_B_nra];
		}
		confDiffHeader = confDiffHeader.trim();
		if (confDiffHeader.length() > 0) confDiffHeader = confDiffHeader.substring(1);
		confDiff = confDiff.trim();
		if (confDiff.length() > 0) confDiff = confDiff.substring(1);
			
		test(confDiffHeader, confDiff, 
				g_d,
				conflictMethods[i_conflictMethod],
				n_A[i_A],
				n_R[i_R],
				n_B_s[i_B_s],
				n_O_s[i_O_s],
				n_G_s[i_G_s],
				n_O_r[i_O_r],
				n_G_r[i_G_r],
				l[i_l],
				n_G_nro[i_G_nro],
				n_B_ra[i_B_ra],
				n_B_nra[i_B_nra],
				beliefAssignment,
				roleAssignment);
	}
	
	public void testAll() throws InvalidConfigurationException {

		for (int i_conflictMethods = 0; i_conflictMethods < conflictMethods.length; i_conflictMethods++) {
			for (int i_A = 0; i_A < n_A.length; i_A++) {
				for (int i_R = 0; i_R < n_R.length; i_R++) {
					for (int i_B_s = 0; i_B_s < n_B_s.length; i_B_s++) {
						for (int i_O_s = 0; i_O_s < n_O_s.length; i_O_s++) {
							for (int i_G_s = 0; i_G_s < n_G_s.length; i_G_s++) {
								for (int i_O_r = 0; i_O_r < n_O_r.length; i_O_r++) {
									for (int i_G_r = 0; i_G_r < n_G_r.length; i_G_r++) {
										for (int i_l = 0; i_l < l.length; i_l++) {
											for (int i_G_nro = 0; i_G_nro < n_G_nro.length; i_G_nro++) {
												for (int i_B_ra = 0; i_B_ra < n_B_ra.length; i_B_ra++) {
													for (int i_B_nra = 0; i_B_nra < n_B_nra.length; i_B_nra++) {

														// Construct a header and the configuration for the variables that actually differ (i.e. are experimented with)
														String confDiffHeader = "";
														String confDiff = "";
														if (conflictMethods.length > 1) {
															confDiffHeader += ScenarioExperiment.COLUMN_SEPARATOR + "Conflict";
															confDiff += ScenarioExperiment.COLUMN_SEPARATOR + conflictMethods[i_conflictMethods];
														}
														if (n_A.length > 1) {
															confDiffHeader += ScenarioExperiment.COLUMN_SEPARATOR + "A";
															confDiff += ScenarioExperiment.COLUMN_SEPARATOR + n_A[i_A];
														}
														if (n_R.length > 1) {
															confDiffHeader += ScenarioExperiment.COLUMN_SEPARATOR + "R";
															confDiff += ScenarioExperiment.COLUMN_SEPARATOR + n_R[i_R];
														}
														if (n_B_s.length > 1) {
															confDiffHeader += ScenarioExperiment.COLUMN_SEPARATOR + "B_s";
															confDiff += ScenarioExperiment.COLUMN_SEPARATOR + n_B_s[i_B_s];
														}
														if (n_O_s.length > 1) {
															confDiffHeader += ScenarioExperiment.COLUMN_SEPARATOR + "O_s";
															confDiff += ScenarioExperiment.COLUMN_SEPARATOR + n_O_s[i_O_s];
														}
														if (n_G_s.length > 1) {
															confDiffHeader += ScenarioExperiment.COLUMN_SEPARATOR + "G_s";
															confDiff += ScenarioExperiment.COLUMN_SEPARATOR + n_G_s[i_G_s];
														}
														if (n_O_r.length > 1) {
															confDiffHeader += ScenarioExperiment.COLUMN_SEPARATOR + "O_r";
															confDiff += ScenarioExperiment.COLUMN_SEPARATOR + n_O_r[i_O_r];
														}
														if (n_G_r.length > 1) {
															confDiffHeader += ScenarioExperiment.COLUMN_SEPARATOR + "G_r";
															confDiff += ScenarioExperiment.COLUMN_SEPARATOR + n_G_r[i_G_r];
														}
														if (l.length > 1) {
															confDiffHeader += ScenarioExperiment.COLUMN_SEPARATOR + "l";
															confDiff += ScenarioExperiment.COLUMN_SEPARATOR + l[i_l];
														}
														if (n_G_nro.length > 1) {
															confDiffHeader += ScenarioExperiment.COLUMN_SEPARATOR + "G_nro";
															confDiff += ScenarioExperiment.COLUMN_SEPARATOR + n_G_nro[i_G_nro];
														}
														if (n_B_ra.length > 1) {
															confDiffHeader += ScenarioExperiment.COLUMN_SEPARATOR + "B_ra";
															confDiff += ScenarioExperiment.COLUMN_SEPARATOR + n_B_ra[i_B_ra];
														}
														if (n_B_nra.length > 1) {
															confDiffHeader += ScenarioExperiment.COLUMN_SEPARATOR + "B_nra";
															confDiff += ScenarioExperiment.COLUMN_SEPARATOR + n_B_nra[i_B_nra];
														}
														confDiffHeader = confDiffHeader.trim();
														if (confDiffHeader.length() > 0) confDiffHeader = confDiffHeader.substring(1);
														confDiff = confDiff.trim();
														if (confDiff.length() > 0) confDiff = confDiff.substring(1);
															
														test(confDiffHeader, confDiff, 
																g_d,
																conflictMethods[i_conflictMethods],
																n_A[i_A],
																n_R[i_R],
																n_B_s[i_B_s],
																n_O_s[i_O_s],
																n_G_s[i_G_s],
																n_O_r[i_O_r],
																n_G_r[i_G_r],
																l[i_l],
																n_G_nro[i_G_nro],
																n_B_ra[i_B_ra],
																n_B_nra[i_B_nra],
																beliefAssignment,
																roleAssignment);
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}

	}

	public void test(List<Pool> agents, String confDiffHeader, String confDiff) {

		// Write header
		for (ScenarioTestListener listener : allTestListeners ) {
			listener.writeHeader(confDiffHeader);
		}

		// Can agents form arguments for their own options?
		// And can agents form counter-arguments to these?
		int totalO = 0;
		int totalArg = 0;
		int totalArgToGd = 0;
		int totalCounterArg = 0;
		int totalCounterArgToGd = 0;
		for (Pool agent : agents) {
			int arg = 0;
			int argToGd = 0;
			int counterArg = 0;
			int counterArgToGd = 0;
			for (Term o : agent.O) {
				
				// Test for arguments from the agent's option to each of its goals
				boolean hasArg = false;
				boolean hasArgToGd = false;
				List<RuleArgument> proofs = new ArrayList<RuleArgument>();
				for (Constant g : agent.G) {
					
					// Now testing for arguments to the mutual goal?
					boolean isToGd = g.isEqualModuloVariables(new Constant(g_d));
					
					// Find proofs
					List<RuleArgument> foundproofs = findProofs(g, agent.B, o);
					if (foundproofs.size() > 0) {

						// Argument for this option found
						proofs.addAll(foundproofs);
						if (printResultsToConsole) {
							System.out.println("  Agent has " + proofs.size() + " argument(s) from " + o.inspect() + " to " + g.inspect() + ":");
							for (RuleArgument proof : proofs) {
								System.out.println("    " + printArgAsChain(proof));
							}
						}
						if (isToGd)
							hasArgToGd = true;
						else
							hasArg = true;
						
					}
				}
				if (hasArg)
					arg++;
				if (hasArgToGd)
					argToGd++;
				if (!hasArg && printResultsToConsole)
					System.out.println("  Agent has no arguments from " + o.inspect() + " to any personal goal");
				if (!hasArgToGd && printResultsToConsole)
					System.out.println("  Agent has no arguments from " + o.inspect() + " to g_d");
				
				// Test for counterarguments to each of the arguments that were found to connect the option to some goal
				boolean hasCounterArg = false;
				boolean hasCounterArgToGd = false;
				for (RuleArgument proof : proofs) {
					
					// Does some agent have a counter-argument to it?
					for (Pool other : agents) {
						if (!other.equals(agent)) {

							// Was this an argument for the mutual goal?
							Constant g = proof.getClaim();
							boolean isToGd = g.isEqualModuloVariables(new Constant(g_d));

							// Find counterproofs
							List<RuleArgument> counter = findCounterArguments(proof, other.B, o);
							if (counter.size() > 0) {
								// Counterargument(s) found
								if (needsToNotHaveAnArgument) {
									// Does this agent have an argument for the option itself
									// (Otherwise we don't consider this counterargument as the agents will not play it)
									List<RuleArgument> proof2 = findProofs(g, other.B, o);
									if (proof2.size() <= 0) {
										if (printResultsToConsole) {
											System.out.println("  And another agent has " + counter.size() + 
													" counterargument(s) (and no arg for option " + o + "):");
											for (RuleArgument count : counter) {
												System.out.println("    " + printArgAsChain(count));
											}
										}
										if (isToGd)
											hasCounterArgToGd = true;
										else
											hasCounterArg = true;
									}
								} else {
									if (printResultsToConsole) {
										System.out.println("  And another agent has " + counter.size() + 
												" counterargument(s):");
										for (RuleArgument count : counter) {
											System.out.println("    " + printArgAsChain(count));
										}
									}
									if (isToGd)
										hasCounterArgToGd = true;
									else
										hasCounterArg = true;
								}
							}
						}
					}
				}
				if (hasCounterArg)
					counterArg++;
				if (hasCounterArgToGd)
					counterArgToGd++;
				if (!hasCounterArg && printResultsToConsole)
					System.out.println("  Agent has no counterarguments from " + o.inspect() + " to any personal goal");
				if (!hasCounterArgToGd && printResultsToConsole)
					System.out.println("  Agent has no counterarguments from " + o.inspect() + " to g_d");
			}
			totalO += agent.O.size();
			totalArg += arg;
			totalArgToGd += argToGd;
			totalCounterArg += counterArg;
			totalCounterArgToGd += counterArgToGd;
			if (printResultsToConsole) {
				System.out.println("Agent has arguments to g_d from " + argToGd + " of its " + agent.O.size() + " options.");
				System.out.println("And other agents have counterarguments for " + counterArgToGd + " of these " + argToGd + " options.");
				System.out.println("Agent has arguments to a personal goal from " + arg + " of its " + agent.O.size() + " options.");
				System.out.println("And other agents have counterarguments for " + counterArg + " of these " + arg + " options.");
			}
		}

		// Register results
		for (ScenarioTestListener listener : allTestListeners) {
			// g_d + " " + n_A + " " + n_R + " " + n_B_s + " " + n_O_s + " " + n_G_s + " " + n_O_r + " " + n_G_r + " " + l + " " + n_G_nro + " " + n_B_ra + " " + n_B_nra + " " + (beliefAssignment == null ? "" : beliefAssignment.name()) + " " + (roleAssignment == null ? "" : roleAssignment.name())
			listener.writeResult("", 
					confDiff == null ? "" : confDiff,
					agents.size(),
					totalO,
					totalArgToGd,
					totalCounterArgToGd,
					totalArg,
					totalCounterArg);
		}
		
	}

	private void test(String confDiffHeader, String confDiff, String g_d, ConflictMethod conflictMethod, int n_A,
			int n_R, int n_B_s, int n_O_s, int n_G_s, int n_O_r, int n_G_r, int l, int n_G_nro,
			int n_B_ra, int n_B_nra, AssignmentMethod beliefAssignment, AssignmentMethod roleAssignment)
			throws InvalidConfigurationException {

		// Show which configuration we are testing
		if (printResultsToConsole ) {
			System.out.println("Test: " + g_d + " " + conflictMethod + " " + n_A + " " + n_R + " " + n_B_s + " "
					+ n_O_s + " " + n_G_s + " " + n_O_r + " " + n_G_r + " " + l + " " + n_G_nro + " "
					+ n_B_ra + " " + n_B_nra + " " + beliefAssignment.name() + " " + roleAssignment.name());
		}

		// Generate scenario
		ScenarioGenerator scenario = new ScenarioGenerator(g_d, n_A, n_R, n_B_s, n_O_s, n_G_s, n_O_r, n_G_r, l,
				n_G_nro, n_B_ra, n_B_nra, beliefAssignment, roleAssignment, conflictMethod);
		List<Pool> agents;
		try {
			agents = scenario.generate();
		} catch (InvalidConfigurationException e) {
			System.out.println("Invalid configuration: " + e.getMessage());
			for (ScenarioTestListener listener : allTestListeners) {
				listener.writeError("Invalid configuration: " + e.getMessage());
			}
			throw e;
		}

		test(agents, confDiffHeader, confDiff);

		if (printResultsToConsole ) {
			System.out.println();
		}
		
	}

	private List<RuleArgument> findCounterArguments(RuleArgument argumentToAttack, List<Constant> b, Term option) {
		KnowledgeBase kb = new KnowledgeBase();
		kb.addRules(b);
		List<RuleArgument> allProofs = new ArrayList<RuleArgument>();
		findCounterArguments(allProofs, argumentToAttack, kb, option);
		return allProofs;
	}
	
	private void findCounterArguments(List<RuleArgument> allProofs, RuleArgument argumentToAttack, KnowledgeBase kb, Term option) {
		try {
			
			//if (argumentToAttack.isAtomic()) {
				allProofs.addAll(helper.findProof(new ConstantList(argumentToAttack.getClaim().negation()), argumentToAttack.getModifier(), kb, Arrays.asList(new Rule(option)), null));
			//}
			
			// Look for a counter-argument to a premise/inference of this argument
			for (RuleArgument subArgument : argumentToAttack.getSubArgumentList().getArguments()) {
				findCounterArguments(allProofs, subArgument, kb, option);
			}
			
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (ReasonerException e) {
			e.printStackTrace();
		}
	}

	private List<RuleArgument> findProofs(Constant goal, List<Constant> b, Term option) {
		KnowledgeBase kb = new KnowledgeBase();
		kb.addRules(b);
		try {
			return helper.findProof(new ConstantList(goal), 0.0, kb, Arrays.asList(new Rule(option)), option);
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (ReasonerException e) {
			e.printStackTrace();
		}
		return null;
	}

	private String printArgAsChain(RuleArgument arg) {
		String out = (!arg.isAtomic() || arg.getClaim() instanceof Term)? 
				arg.getClaim().inspect() + " <- ": "";
		for (RuleArgument sub : arg.getSubArgumentList().getArguments()) {
			out += printArgAsChain(sub);
		}
		return out;
	}
	
}