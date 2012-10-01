package nl.uu.cs.arg.exp.result;

import java.io.File;

/**
 * Factory for local implementations of experiment result writers.
 * New implementations of that interface should be added here and
 * be instantiated using create();
 * 
 * Any implementer's initialise() method will be called, but null
 * will be supplied (instead of a {@link File}) if needsFile()
 * returns null in this enumeration.
 * 
 * @author erickok
 */
public enum LocalExperimentWriter {

	Console {
		@Override
		public ExperimentResultWriter create() { return new ConsoleResultWriter(); }
		@Override
		public boolean needsFile() { return false; }
	},
	Csv {
		@Override
		public ExperimentResultWriter create() { return new CsvResultWriter(); }
		@Override
		public boolean needsFile() { return true; }
	};
	
	public abstract ExperimentResultWriter create();
	public abstract boolean needsFile();
	
}
