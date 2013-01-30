package nl.uu.cs.arg.exp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;
import nl.uu.cs.arg.exp.result.DialogueStats;
import nl.uu.cs.arg.exp.result.ExperimentResultWriter;
import nl.uu.cs.arg.exp.result.LocalExperimentWriter;
import nl.uu.cs.arg.exp.scenario.ScenarioExperiment;
import nl.uu.cs.arg.exp.scenario.ScenarioExperiment.ScenarioTestCsvWriter;
import nl.uu.cs.arg.exp.scenario.ScenarioTester;
import nl.uu.cs.arg.platform.PlatformOutputLevel;
import nl.uu.cs.arg.platform.PlatformOutputPrinter;
import nl.uu.cs.arg.platform.Settings;
import nl.uu.cs.arg.platform.local.AgentXmlData;
import nl.uu.cs.arg.platform.local.BDIAgent;
import nl.uu.cs.arg.platform.local.BDIAgent.Property;
import nl.uu.cs.arg.platform.local.LocalAgent;
import nl.uu.cs.arg.platform.local.MasXmlData;
import nl.uu.cs.arg.platform.local.ValuedGoal;
import nl.uu.cs.arg.shared.Agent;
import nl.uu.cs.arg.shared.dialogue.Goal;
import nl.uu.cs.arg.shared.dialogue.protocol.DeliberationRule;
import nl.uu.cs.arg.shared.dialogue.protocol.OutcomeSelectionRule;
import nl.uu.cs.arg.shared.dialogue.protocol.TerminationRule;
import nl.uu.cs.arg.shared.scenario.ScenarioGenerator;
import nl.uu.cs.arg.shared.scenario.ScenarioGenerator.ConflictMethod;
import nl.uu.cs.arg.shared.scenario.ScenarioGenerator.InvalidConfigurationException;
import nl.uu.cs.arg.shared.scenario.ScenarioGenerator.Pool;

import org.aspic.inference.Constant;
import org.aspic.inference.KnowledgeBase;
import org.aspic.inference.Rule;
import org.aspic.inference.Term;
import org.aspic.inference.Variable;

public class Experiment implements DialogueMonitor {

	private PlatformOutputPrinter dialogueOutputPrinter;
	private final int runs;
	private final List<ExperimentResultWriter> experimentResultWriters;
	private File historyDirectory;
	private final List<BDIAgent.Property> strategyPropertiesToTest;
	private OutcomeSelectionRule outcomeSelectionRule = OutcomeSelectionRule.FirstThatIsIn;
	private final LocalAgent agentStrategy;

	public Experiment(int runs, LocalAgent strategy) {
		this.runs = runs;
		this.experimentResultWriters = new ArrayList<ExperimentResultWriter>();
		this.strategyPropertiesToTest = new ArrayList<Property>();
		this.agentStrategy = strategy;
	}

	public void setDialogueOutputPrinter(PlatformOutputPrinter dialogueOutputPrinter) {
		this.dialogueOutputPrinter = dialogueOutputPrinter;
	}

	public void addExperimentResultWriter(ExperimentResultWriter writer) {
		// Copy the names of the strategy properties to test so we can output them later
		List<String> props = new ArrayList<String>();
		for (Property prop : strategyPropertiesToTest) {
			props.add(prop.name());
		}
		writer.setStrategyPropertiesOutput(props );
		this.experimentResultWriters.add(writer);
	}

	private void setHistoryDirectory(File historyDirectory) {
		this.historyDirectory = historyDirectory;
	}

	public void addStrategyPropertiesToTest(Property prop) {
		this.strategyPropertiesToTest.add(prop);
	}

	private void setOutcomeSelectionRuleToTest(OutcomeSelectionRule outcomeSelectionRule) {
		this.outcomeSelectionRule = outcomeSelectionRule;
	}

	private void start() {

		try {

			String topic = "do";
			String goal = "g_d";
	        Term topicTerm = new Term(topic, new Variable("T"));
	        Goal topicGoal = new Goal(new Constant(goal));

	        ScenarioTestCsvWriter testWriter = null;
	        if (historyDirectory != null) {
	        	testWriter = new ScenarioExperiment.ScenarioTestCsvWriter(new File(historyDirectory.toString() + File.separator + "ScenarioTest.txt"), true);
	        }
	        
			for (int runId = 0; runId < runs; runId++) {

				// Use the standard settings
				List<DeliberationRule> deliberationRules = Arrays.asList(//DeliberationRule.AttackOnOwnMove,
						DeliberationRule.NoRepeatInBranch);
				List<TerminationRule> terminationRules = Arrays.asList(TerminationRule.NoParticipants,
						TerminationRule.InactiveRound);
				Settings settings = new Settings(deliberationRules, terminationRules, outcomeSelectionRule);

				// Generate a configuration for every property we want to test
				ArrayList<BitSet> configs = new ArrayList<BitSet>();
				for (int i = 0; i < (Math.pow(2, strategyPropertiesToTest.size())); i++) {
					configs.add(createBits(i));
				}

				// Generate the scenario
				/*final int players = 2;
				final int optionsCount = 17; // Also used to format the dialogue output
				ScenarioGenerator gen = new ScenarioGenerator(goal, 
						players,		// n_A
						6, 				// n_R
						10, 			// n_B_s
						optionsCount,	// n_O_s
						12,	 			// n_G_s
						10,				// n_O_r
						5,				// n_G_r
						2,				// l
						5, 				// n_G_nro
						35, 			// n_B_ra
						25,	 			// n_B_nra
						ScenarioGenerator.AssignmentMethod.Randomly, 
						ScenarioGenerator.AssignmentMethod.Evenly,
						ConflictMethod.Chained);*/
				final int players = 7;
				final int optionsCount = 17; // Also used to format the dialogue output
				ScenarioGenerator gen = new ScenarioGenerator(goal, 
						players,		// n_A
						8, 				// n_R
						40, 			// n_B_s
						optionsCount,	// n_O_s
						12,	 			// n_G_s
						8,				// n_O_r
						5,				// n_G_r
						2,				// l
						2, 				// n_G_nro
						60, 			// n_B_ra
						35,	 			// n_B_nra
						ScenarioGenerator.AssignmentMethod.Randomly, 
						ScenarioGenerator.AssignmentMethod.Evenly,
						ConflictMethod.Chained);
				List<Pool> scenario = null;
				try {
					scenario = gen.generate();
				} catch (InvalidConfigurationException e) {
					System.out.println("Run " + runId + " skipped! Invalid scenario configuration: " + e.getMessage());
					runId--;
					if (testWriter != null) {
						testWriter.writeError(e.getMessage());
					}
				}
				if (scenario == null) {
					continue;
				}
				
				// Test it
				ScenarioTester test = new ScenarioTester(goal, false);
				if (testWriter != null) {
					test.addListener(testWriter);
				}
				test.test(scenario, "", "");
				
				// Loop over the strategy properties to test
				int configId = 0;
				for (BitSet config : configs) {

					HashMap<String, Object> strategy = new HashMap<String, Object>();
					int c = 0;
					for (BDIAgent.Property prop : strategyPropertiesToTest) {
						strategy.put(prop.name(), config.get(c));
						c++;
					}

					// Generate agents
					List<Agent> agents = new ArrayList<Agent>();
					List<AgentXmlData> agentXml = new ArrayList<AgentXmlData>();
					for (int i = 0; i < players; i++) {
						// Create knowledge base object
						KnowledgeBase kb = new KnowledgeBase();
						kb.addRules(scenario.get(i).B);
						// Create option objects for the option terms
						ArrayList<Rule> options = new ArrayList<Rule>();
						for (Term o : scenario.get(i).O) {
							options.add(new Rule(o));
						}
						// Create goal objects for the goal constants
						// This also assigns a utility of 1, ..., n_G_r
						// TODO?: Improve this method? Don't assign g_d a utility?
						ArrayList<Goal> goals = new ArrayList<Goal>();
						int u = 1;
						for (Constant g : scenario.get(i).G) {
							goals.add(new ValuedGoal(g, u));
							u++;
						}
						// Create the agent objects
						AgentXmlData a = new AgentXmlData("Agent" + i, kb, options, goals, new ArrayList<Goal>(), strategy);
						agents.add(agentStrategy.createAgent(a));
						agentXml.add(a);

					}

					if (historyDirectory != null) {
						// Write this generated dialogue to XML file(s)
						File masFile = new File(historyDirectory.toString() + File.separator + "Exp" + runId + "-Conf"
								+ configId + File.separator + "Mas.baidd");
						new MasXmlData(topicTerm, topicGoal, agents, agentXml, deliberationRules, terminationRules,
								outcomeSelectionRule).saveMasDataToXml(masFile, true);
					}

					// Start run
					// Note that this starting of the run is synchronous
					// although the platform itself will actually run
					// asynchronously
					DialogueRun run = new DialogueRun(runId, configId, this, settings, optionsCount, agentStrategy.name(), dialogueOutputPrinter);
					run.start(topicTerm, topicGoal, agents);

					configId++;
				}

			}

			// Allow the writers to finalize, if needed
			for (ExperimentResultWriter writer : experimentResultWriters) {
				writer.finalise();
			}
			if (testWriter != null) {
				testWriter.finalise();
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		}

	}

	private BitSet createBits(int value) {
		BitSet bits = new BitSet(Integer.SIZE);
		int index = 0;
		while (value != 0) {
		  if (value % 2 != 0) {
		    bits.set(index);
		  }
		  ++index;
		  value = value >>> 1;
		}
		return bits;
	}

	@Override
	public void dialogueTerminated(DialogueStats stats) {

		for (ExperimentResultWriter writer : experimentResultWriters) {
			try {
				writer.writeResult(stats);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * Main entry point to run an experiment
	 * @param args Command line arguments
	 */
	public static void main(String[] args) {

		// Create a parser for the command line arguments
		OptionParser parser = new OptionParser();
		ArgumentAcceptingOptionSpec<PlatformOutputLevel> levelOption = parser
				.acceptsAll(java.util.Arrays.asList("l", "level"),
						"Set message console print level {Exceptions, Messages, Moves}").withRequiredArg()
				.ofType(PlatformOutputLevel.class);
		ArgumentAcceptingOptionSpec<LocalAgent> agentOption = parser
				.acceptsAll(java.util.Arrays.asList("s", "strategy"),
						"Set agent strategy {BDIAgent, NonArguingAgent}").withRequiredArg()
				.ofType(LocalAgent.class);
		ArgumentAcceptingOptionSpec<LocalExperimentWriter> outputOption = parser
				.acceptsAll(java.util.Arrays.asList("o", "output"), "Set the experiment results output {Console, Csv}")
				.withRequiredArg().ofType(LocalExperimentWriter.class);
		ArgumentAcceptingOptionSpec<File> fileOption = parser
				.acceptsAll(java.util.Arrays.asList("f", "file"), "File to write output to").withRequiredArg()
				.ofType(File.class);
		ArgumentAcceptingOptionSpec<Integer> runsOption = parser
				.acceptsAll(java.util.Arrays.asList("r", "runs"), "Number of dialogues to run").withRequiredArg()
				.ofType(Integer.class);
		ArgumentAcceptingOptionSpec<File> historyOption = parser
				.acceptsAll(java.util.Arrays.asList("y", "history"), "Directory to store history of generated agents")
				.withRequiredArg().ofType(File.class);
		ArgumentAcceptingOptionSpec<BDIAgent.Property> stratOption = parser
				.acceptsAll(java.util.Arrays.asList("p", "property"), "Experiment with a certain BDI strategy property")
				.withRequiredArg().ofType(BDIAgent.Property.class);
		ArgumentAcceptingOptionSpec<OutcomeSelectionRule> outcomeOption = parser
				.acceptsAll(java.util.Arrays.asList("O", "outcomerule"), "Use a specific outcome selection rule")
				.withRequiredArg().ofType(OutcomeSelectionRule.class);
		OptionSpecBuilder helpOption = parser.acceptsAll(java.util.Arrays.asList("?", "h", "help"),
				"Print this usage message");
		OptionSpecBuilder versionOption = parser.acceptsAll(java.util.Arrays.asList("v", "version"), "Version info");

		try {

			// Parse the arguments
			OptionSet options = parser.parse(args);

			// Usage or version information
			if (options == null || options.has(helpOption)) {
				printCommandLineUsage(parser);
				return;
			} else if (options.has(versionOption)) {
				System.out.println(Settings.APPLICATION_NAME_VERSION);
				return;
			}

			// Number of runs
			int runs = 0; // Default to just 1 run
			if (options.hasArgument(runsOption)) {
				runs = runsOption.value(options);
			}
			if (runs <= 0) {
				printCommandLineUsage(parser, "Specify the number of runs, for example:\n" + "\t-r 100");
				return;
			}

			// Agent strategy
			LocalAgent strategy = null; // Default to InactiveAgent
			if (options.hasArgument(agentOption)) {
				strategy = agentOption.value(options);
			}
			if (strategy == null) {
				printCommandLineUsage(parser, "Specify the agent strategy, for example:\n" + "\t-s BDIAgent");
				return;
			}

			// Initialise the experiment settings
			Experiment experiment = new Experiment(runs, strategy);

			// Console message printing level
			PlatformOutputPrinter platformPrinter = null;
			if (options.hasArgument(levelOption)) {
				platformPrinter = PlatformOutputPrinter.defaultPlatformOutputPrinter;
				platformPrinter.setLevel(levelOption.value(options));
				experiment.setDialogueOutputPrinter(platformPrinter);
			}

			// Output a history of generated agents?
			File historyDirectory = null;
			if (options.hasArgument(historyOption)) {
				historyDirectory = historyOption.value(options);
				if (!historyDirectory.exists()) {
					printCommandLineUsage(parser, "Directory to store the generated agents history does not exist");
					return;
				}
				if (!historyDirectory.isDirectory()) {
					printCommandLineUsage(parser,
							"Supplied path to store the generated agents history is not a directory");
					return;
				}
				experiment.setHistoryDirectory(historyDirectory);
			}

			// Experiment with certain BDI strategy properties?
			if (options.hasArgument(stratOption)) {
				for (BDIAgent.Property prop : stratOption.values(options)) {
					experiment.addStrategyPropertiesToTest(prop);
				}
			}

			// Use a specific outcome selection protocol rule?
			if (options.hasArgument(outcomeOption)) {
				experiment.setOutcomeSelectionRuleToTest(outcomeOption.value(options));
			}

			// Experiment output
			if (options.hasArgument(fileOption) || options.hasArgument(outputOption)) {
				Iterator<File> fileOptionI = fileOption.values(options).iterator();
				List<LocalExperimentWriter> writerOptions = outputOption.values(options);
				for (LocalExperimentWriter writerOption : writerOptions) {

					// Check of this writer type needs an output file specified
					if (writerOption.needsFile()) {
						if (fileOptionI.hasNext()) {

							// Create the writer with its attached file
							ExperimentResultWriter writer = writerOption.create();
							writer.initialise(fileOptionI.next(), false);
							experiment.addExperimentResultWriter(writer);

						} else {

							printCommandLineUsage(parser, "Not enough files specified.\n"
									+ "Specify one file for every output type that needs one, for example:\n"
									+ "\t-o Csv -f out1.csv -o Csv -f out2.csv -o Console");
							return;

						}
					} else {

						// Create the writer (without a file attached)
						ExperimentResultWriter writer = writerOption.create();
						writer.initialise(null, false);
						experiment.addExperimentResultWriter(writer);

					}

				}

				// Any left-over files specified?
				if (fileOptionI.hasNext()) {
					printCommandLineUsage(parser, "Files specified without an output type.\n"
							+ "Specify one file for every output type that needs one, for example:\n"
							+ "\t-o Csv -f out1.csv -o Csv -f out2.csv -o Console");
					return;
				}
			}

			// Start the experiment
			printAppHeader();
			experiment.start();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (OptionException e) {
			try {
				printCommandLineUsage(parser, e.getMessage());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

	}

	private static void printCommandLineUsage(OptionParser parser) throws IOException {
		printCommandLineUsage(parser, null);
	}

	private static void printCommandLineUsage(OptionParser parser, String startupErrorMessage) throws IOException {

		printAppHeader();

		// Exception to show?
		if (startupErrorMessage != null) {
			System.out.println(startupErrorMessage);
			System.out.println();
		}

		// Usage message
		String startupScriptName = "./baidd-exp";
		if (File.separator.equals("\\")) { // Windows systems
			startupScriptName = "baidd-exp.bat";
		}
		System.out.println("Usage:");
		System.out.println("\t" + startupScriptName + " [option]...");
		System.out.println("For example, start an experiment with verbose output and 10 runs:");
		System.out.println("\t" + startupScriptName + " -l Moves -o Console -o Csv -f output.csv -r 10");
		System.out.println();

		// Command line help directly form the parser specification
		parser.printHelpOn(System.out);

	}

	private static void printAppHeader() {

		// Application welcome message
		String appName = Settings.APPLICATION_NAME_VERSION + " experiment";
		System.out.println(appName);
		char[] hyphens = new char[appName.length()];
		Arrays.fill(hyphens, '-'); // Used to create a hyphen --- line of the
									// same length as the application name
		System.out.println(String.valueOf(hyphens));
		System.out.println();

	}

}
