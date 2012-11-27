package nl.uu.cs.arg.exp.result;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

import org.aspic.inference.Constant;

/**
 * An experiment result writer that writes the dialogue statistics 
 * to a 'comma' separated values (CSV) file. By default the column
 * separator is a ; (NOT a ,) and line are ended with LF (Linux
 * style).
 * 
 * This writer assumes the number of participants stays constant
 * over multiple dialogues as well as their order.
 * 
 * @author erickok
 */
public class CsvResultWriter implements ExperimentResultWriter {

	private static final String LINE_SEPARATOR = "\n";
	private static final String COLUMN_SEPARATOR = ";";
	
	private File outFile;
	private FileWriter outWriter;
	private boolean headerWritten;
	private List<String> strategyPropertiesToTest = null;
	
	@Override
	public void initialise(File file, boolean append) throws IOException {
		this.outFile = file;
		
		// Empty or create the requested file for output
		headerWritten = outFile.exists() && append;
		if (outFile.exists() && !append) {
			outFile.delete();
		}
		if (outFile.getParentFile() != null) {
			outFile.getParentFile().mkdirs();
		}
		if (!outFile.exists() || !append) {
			outFile.createNewFile();
		}
		
		// Create write handle
		outWriter = new FileWriter(outFile, append);
	}

	@Override
	public void setStrategyPropertiesOutput(List<String> strategyPropertiesToTest) {
		this.strategyPropertiesToTest  = strategyPropertiesToTest;
	}

	@Override
	public void writeResult(DialogueStats stats) throws IOException {
		
		// Write header?
		if (!headerWritten) {
			w("ID");
			w("File");
			w("Config");
			w("Strategy");
			//w("Distribution");
			w("OutcomeRule");
			w("Date");
			w("O");
			
			if (strategyPropertiesToTest != null && stats.stratprops != null) {
				for (Entry<String, Object> p : stats.stratprops.entrySet()) {
					if (strategyPropertiesToTest.contains(p.getKey())) {
						w(p.getKey());
					}
				}
			}
			/*w("PoolBeliefs");
			w("PoolRules");
			w("FactNegation");
			w("AgentBeliefs");
			w("AgentRules");*/
			
			w("e_move");
			w("e_relevance^strong");
			w("e_concealment");
			w("e_total^avg");
			w("e_total^avg_in");
			w("e_total^o");
			w("e_pareto^o");

			for (int i = 0; i < stats.optionsCount; i++) {
				w("e_total^" + i);
			}
			next();
			
			headerWritten = true;
		}
		
		// Write a single dialogue's statistics
		w(stats.id);
		w(outFile.getParentFile().getName());
		w(stats.configId);
		w(stats.agentStrategy);
		//w(stats.distribution.id);
		w(stats.settings.getOutcomeSelectionRule().name());
		w(stats.date.toString());
		w((stats.o == null? "": stats.o.inspect()));
		
		if (strategyPropertiesToTest != null && stats.stratprops != null) {
			for (Entry<String, Object> p : stats.stratprops.entrySet()) {
				if (strategyPropertiesToTest.contains(p.getKey())) {
					w(p.getValue().toString());
				}
			}
		}
		/*w(stats.distribution.poolBeliefsString());
		w(stats.distribution.poolRulesString());
		w(stats.distribution.factNegationString());
		w(stats.distribution.agentBeliefsString());
		w(stats.distribution.agentRulesString());*/

		w(stats.e_moves);
		w(stats.e_strongrelevance);
		w(stats.e_concealment);
		w(stats.e_total_avg);
		w(stats.e_total_in_avg);
		w(stats.e_total_o);
		w(stats.e_pareto_o);

		for (Entry<Constant, Integer> p : stats.e_totalutility.entrySet()) {
			w(p.getValue());
		}
		next();
		
		outWriter.flush();
	}

	private void w(Integer i) throws IOException {
		w(i == null? "": Integer.toString(i));
	}
	private void w(Float f) throws IOException {
		w(f == null? "": Float.toString(f));
	}
	private void w(Boolean b) throws IOException {
		w(b == null? "": Boolean.toString(b));
	}
	private void w(String s) throws IOException {
		outWriter.write(s);
		outWriter.write(COLUMN_SEPARATOR);
	}
	private void next() throws IOException {
		outWriter.write(LINE_SEPARATOR);
	}
	
	@Override
	public void finalise() throws IOException {
		// Close any connection to the output file
		if (outWriter != null) {
			outWriter.close();
		}
	}

}
