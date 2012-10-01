package nl.uu.cs.arg.platform;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;
import nl.uu.cs.arg.platform.local.MasXmlData;

/**
 * Provides a public way to start an asynchronous platform. This includes
 * a command line application with based output support.
 * 
 * @author erickok
 *
 */
public class Launcher {

	Thread platformThread;
	Platform platform;
	MasXmlData masXmlData;
	
	/**
	 * @param args Command line startup arguments
	 */
	public static void main(String[] args) {

		// Create a parser for the command line arguments
		OptionParser parser = new OptionParser();
		ArgumentAcceptingOptionSpec<PlatformOutputLevel> levelOption = parser.
			acceptsAll(java.util.Arrays.asList("l", "level"), "Set message console print level {Exceptions, Messages, Moves}").
			withRequiredArg().ofType(PlatformOutputLevel.class);
		OptionSpecBuilder helpOption = parser.acceptsAll(java.util.Arrays.asList("?", "h", "help"), "Print this usage message");
		OptionSpecBuilder versionOption = parser.acceptsAll(java.util.Arrays.asList("v", "version"), "Version info");

		try {

			// Parse the arguments
			OptionSet options = null;
			try {
				options = parser.parse(args);
			} catch (OptionException e) {
	        	printCommandLineUsage(parser, e.getMessage());
	        	return;
			}

			// Usage or version information
	        if (options == null || options.has(helpOption)) {
	        	printCommandLineUsage(parser);
	        	return;
	        } else if (options.has(versionOption)) {
	        	System.out.println(Settings.APPLICATION_NAME_VERSION);
	        	return;
	        }
	        
	        // Build platform settings object from the supplied command line options
	        List<PlatformListener> listeners = new ArrayList<PlatformListener>();

	        // Console message printing level
	        try {
		        if (options.hasArgument(levelOption)) {
		        	listeners.add(PlatformOutputPrinter.defaultPlatformOutputPrinter);
		        	PlatformOutputPrinter.defaultPlatformOutputPrinter.setLevel(levelOption.value(options));
		        }
	        } catch (OptionException e) {
	        	printCommandLineUsage(parser, e.getMessage());
	        	return;
	        }
	        
	        // Check remaining arguments for a MAS filename
	        List<String> noargs = options.nonOptionArguments();
	        File masFile = null;
	        if (noargs != null && noargs.size() > 0) {
	        	masFile = new File(noargs.get(0));
	        }
	        if (masFile == null || !masFile.exists()) {
	        	// No MAS file specification supplied; print out help info
	        	printCommandLineUsage(parser, "No .baidd MAS XML file specified or file could not be found.");
	        	return;
	        }
	        
	        // Read the MAS XML file (and agent XML files that are specified there)
	        MasXmlData mas = null;
			try {
				mas = MasXmlData.loadAgentDataFromXml(masFile);
			} catch (Exception e) {
				System.out.println("An error occured during the parsing of the MAS and agent XML files.");
				e.printStackTrace();
				return;
			}

			// Copy the read settings to the platform settings object
	        Settings settings = new Settings(mas.getDeliberationRules(), 
	        		mas.getTerminationRules(), mas.getOutcomeSelectionRule());
	        
	        // Start the agent platform
	        Launcher launcher = new Launcher(settings, mas, listeners);
	        launcher.initPlatform();
	        launcher.startPlatform();
	        
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static void printCommandLineUsage(OptionParser parser) throws IOException {
		printCommandLineUsage(parser, null);
	}
	
	private static void printCommandLineUsage(OptionParser parser, String startupErrorMessage) throws IOException {
		
		// Application welcome message
    	System.out.println(Settings.APPLICATION_NAME_VERSION);
    	char[] hyphens = new char[Settings.APPLICATION_NAME_VERSION.length()];
    	Arrays.fill(hyphens, '-'); // Used to create a hyphen --- line of the same length as the application name and version number
    	System.out.println(String.valueOf(hyphens));
    	System.out.println();
    	
    	// Exception to show?
    	if (startupErrorMessage != null) {
        	System.out.println(startupErrorMessage);
        	System.out.println();
    	}
    	
    	// Usage message
		String startupScriptName = "./baidd-console";
		if (File.separator.equals("\\")) { // Windows systems
			startupScriptName = "baidd-console.bat";
		}
    	System.out.println("Usage:");
    	System.out.println("\t" + startupScriptName + " [option]... [masfile]");
    	System.out.println("For example, start the platform for some MAS specification with verbose output:");
    	System.out.println("\t." + startupScriptName + " -l Moves " + File.separator + "path" + File.separator + "to" + File.separator + "TestMas.baidd");
    	System.out.println();
    	
    	// Command line help directly form the parser specification
		parser.printHelpOn(System.out);
		
	}

	/**
	 * Create a platform launcher with specific settings and load the specified agents
	 * @param settings The settings to initialize the platform with
	 * @param listeners A set of {@link PlatformListener}s to attach to the new-to-be-created platform
	 * @param masFile The multi-agent system specification XML file with agent to load
	 */
	public Launcher(Settings settings, MasXmlData masXmlData, List<PlatformListener> listeners) {
		this.platform =  new Platform(settings);
		this.platformThread = new Thread(platform);
		this.masXmlData = masXmlData;
		
		// Add platform listeners
		if (listeners != null) {
			for (PlatformListener listener : listeners) {
				this.platform.addListener(listener);
			}
		}
	}

	public void initPlatform() {
		platform.init(masXmlData.getTopic(), masXmlData.getTopicGoal(), masXmlData.getLocalAgents());
		platformThread.start();		
	}

	public void startPlatform() {
		synchronized (platform) {
			try {
				// Wait until it is started and ready
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
			platform.requestStart();
		}		
	}

	public void pausePlatform() {
		synchronized (platform) {
			platform.requestPause();
		}
	}
	
	public boolean isPlatformPaused() {
		synchronized (platform) {
			return platform.isPaused();
		}
	}

	public void setPlatformSingleStepping(boolean stopAfterEachStep) {
		synchronized (platform) {
			platform.setSingleStepping(stopAfterEachStep);
		}
	}

}
