package nl.uu.cs.arg.exp.result;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import nl.uu.cs.arg.shared.Participant;

import org.aspic.inference.Constant;

/**
 * An experiment result writer that dumps the statistics for each
 * ran dialogue to the console (or rather: the STDOUT).
 * 
 * @author erickok
 */
public class ConsoleResultWriter implements ExperimentResultWriter {

	private List<String> strategyPropertiesToTest = null;
	
	@Override
	public void initialise(File file, boolean append) throws IOException {
		// No initialization needed (and file is ignored)
	}

	@Override
	public void setStrategyPropertiesOutput(List<String> strategyPropertiesToTest) {
		this.strategyPropertiesToTest  = strategyPropertiesToTest;
	}

	@Override
	public void writeResult(DialogueStats stats) throws IOException {

		System.out.println("Run ID: " + stats.id);
		System.out.println("Date: " + stats.date.toString());
		System.out.println(stats.dialogue.prettyPrint());
		for (Entry<Participant, Map<Constant, Integer>> p : stats.utilities.entrySet()) {
			for (Entry<Constant, Integer> q : p.getValue().entrySet()) {
				System.out.println("u_" + p.getKey().getName() + "^" + q.getKey().inspect() + " = " + q.getValue());
			}
		}

		System.out.println();
		//System.out.println("Distribution: " + stats.distribution.toString());
		if (strategyPropertiesToTest != null) {
			System.out.println("Strategy properties:");
			if (stats.stratprops != null) {
				for (Entry<String, Object> prop : stats.stratprops.entrySet()) {
					if (strategyPropertiesToTest.contains(prop.getKey())) {
						System.out.println("  " + prop.getKey() + " = " + prop.getValue());
					}
				}
			}
		}
		
		System.out.println();
		System.out.println("e_move = " + stats.e_moves);
		System.out.println("e_relevance^strong = " + stats.e_strongrelevance);
		System.out.println("e_concealment = " + stats.e_concealment);
		System.out.println("e_total^avg = " + stats.e_total_avg);
		System.out.println("e_total^avg_in = " + stats.e_total_in_avg);
		System.out.println("e_total_o = " + stats.e_total_o);
		System.out.println("e_pareto_o = " + stats.e_pareto_o.toString());
		for (Entry<Constant, Integer> p : stats.e_totalutility.entrySet()) {
			System.out.println("e_total^" + p.getKey().inspect() + " = " + p.getValue().toString());
		}
		for (Entry<Constant, Boolean> p : stats.e_pareto.entrySet()) {
			System.out.println("e_pareto^" + p.getKey().inspect() + " = " + p.getValue().toString());
		}
		System.out.println();
		
	}

	@Override
	public void finalise() throws IOException {
	}

}
