package nl.uu.cs.arg.exp.result;

import java.io.File;
import java.io.IOException;
import java.util.List;


/**
 * Interface for classes that can write dialogue run results in the form
 * of the dialogue statistics. Implementers can write these to a file or
 * database, for example.
 * 
 * @author erickok
 */
public interface ExperimentResultWriter {

	public abstract void initialise(File file, boolean append) throws IOException;	
	public abstract void setStrategyPropertiesOutput(List<String> strategyPropertiesToTest);
	public abstract void writeResult(DialogueStats stats) throws IOException;
	public abstract void finalise() throws IOException;	
	
}
