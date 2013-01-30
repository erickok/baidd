package nl.uu.cs.arg.exp.scenario;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import nl.uu.cs.arg.exp.scenario.ScenarioTester.ScenarioTestListener;
import nl.uu.cs.arg.shared.scenario.ScenarioGenerator.AssignmentMethod;
import nl.uu.cs.arg.shared.scenario.ScenarioGenerator.ConflictMethod;
import nl.uu.cs.arg.shared.scenario.ScenarioGenerator.InvalidConfigurationException;

public class ScenarioExperiment {

	static final String LINE_SEPARATOR = "\n";
	static final String COLUMN_SEPARATOR = ";";

	public static void main(String[] args) {

		int runs = 2000;
		int maxFails = runs * 10;
		boolean testAll = false; // If not all, then only a single random setting is tested
		
		ScenarioTestListener csvWriter = null;
		try {
			csvWriter = new ScenarioTestCsvWriter(
					new File("/home/eric/Dev/baidd/BaiddTest/scenario/test2/ScenarioExp.csv"),
					false);
	
			// Perform the runs to test a certain scenario configuration
			int fails = 0;
			for (int r = 0; r < runs; r++) {
				System.out.println("Test run: " + r);
				ScenarioTester tester = new ScenarioTester("g_d", 
						new int[] { 1,2,3,4,5,6,7,8,9,10 }, // n_A 
						new int[] { 1,2,3,4,5,6,7,8,9,10 }, // n_R
						new int[] { 10,20,30,40,50,60,70,80,90,100,110,120,130,140,150 }, // n_B_s
						new int[] { 2,4,6,8,10,12,14,16,18,20,22,24,26,28,30,32,34,36,38,40 }, // n_O_s
						new int[] { 2,4,6,8,10,12,14,16,18,20,22,24,26,28,30,32,34,36,38,40 }, // n_G_s
						new int[] { 1,2,3,4,5,6,7,8,9,10 }, // n_O_r
						new int[] { 1,2,3,4,5,6,7,8,9,10 }, // n_G_r
						new int[] { 1,2,3,4,5,6,7,8,9,10 }, // l 
						new int[] { 0,1,2,3,4,5,6,7,8,9,10 }, // n_G_nro
						new int[] { 5,10,15,20,25,30,35,40,45,50,55,60,65,70,75,80,85,90,95,100 }, // n_B_ra
						new int[] { 0,5,10,15,20,25,30,35,40,45,50 }, // n_B_nra
						AssignmentMethod.Randomly, 
						AssignmentMethod.Evenly, 
						new ConflictMethod[] { ConflictMethod.Direct, ConflictMethod.Chained }, 
						false,
						true);
				tester.addListener(csvWriter);
				try {
					if (testAll)
						tester.testAll();
					else
						tester.testRandomSetting();
				} catch (InvalidConfigurationException e) {
					// This configuration didn't work out; don't consider this as a generated run
					// to make sure 'runs' runs are generated in total
					r--;
					// Register this and crash if it happens to often, e.g. more than the desired number of runs
					fails++;
					if (fails > maxFails) {
						//throw new RuntimeException("Generating runs with this configuration (" + tester.toString() + ") seems too hard or is impossible. Force stopping now!");
					}
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
			return;
		} finally {
			if (csvWriter != null)
				csvWriter.finalise();
		}
	}

	public static class ScenarioTestCsvWriter implements ScenarioTestListener {

		private final boolean writeErrors;
		private final FileWriter outWriter;
		private boolean writtenHeader = false;

		public ScenarioTestCsvWriter(File outCsvFile, boolean writeErrors) throws IOException {

			this.writeErrors = writeErrors;
			
			// Setup output directory
			if (!outCsvFile.getParentFile().exists()) {
				outCsvFile.getParentFile().mkdirs();
			}

			// Create and open output file
			if (outCsvFile.exists()) {
				outCsvFile.delete();
			}
			outCsvFile.createNewFile();
			outWriter = new FileWriter(outCsvFile);
		}

		@Override
		public void writeHeader(String confDiffHeader) {
			// Write header
			if (!writtenHeader) {
				writtenHeader = true;
					writeRow("Date", "Configuration", confDiffHeader, "A", "O",
							"argToGd", "counterArgToGd", "arg", "counterArg");
			}
		}
		@Override
		public void writeResult(String configuration, String confDiff, int totalA,
				int totalO, int totalArgToGd, int totalCounterArgToGd, int totalArg, int totalCounterArg) {
			writeRow(new Date().toString(), configuration, confDiff, "" + totalA, "" + totalO, "" + totalArgToGd, ""
					+ totalCounterArgToGd, "" + totalArg, "" + totalCounterArg);
		}

		@Override
		public void writeError(String errorText) {
			if (writeErrors)
				writeRow(new Date().toString(), errorText, "", "", "", "", "", "", "");
		}

		private void writeRow(String date, String col1, String col2,
				String col3, String col4, String col5, String col6, String col7, String col8) {
			try {
				outWriter.write(date);
				outWriter.write(COLUMN_SEPARATOR);
				outWriter.write(col1);
				outWriter.write(COLUMN_SEPARATOR);
				outWriter.write(col2);
				outWriter.write(COLUMN_SEPARATOR);
				outWriter.write(col3);
				outWriter.write(COLUMN_SEPARATOR);
				outWriter.write(col4);
				outWriter.write(COLUMN_SEPARATOR);
				outWriter.write(col5);
				outWriter.write(COLUMN_SEPARATOR);
				outWriter.write(col6);
				outWriter.write(COLUMN_SEPARATOR);
				outWriter.write(col7);
				outWriter.write(COLUMN_SEPARATOR);
				outWriter.write(col8);
				outWriter.write(LINE_SEPARATOR);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void finalise() {
			// Close any connection to the output file
			if (outWriter != null) {
				try {
					outWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

}
