package nl.uu.cs.arg.exp;

import nl.uu.cs.arg.exp.result.DialogueStats;

public interface DialogueMonitor {

	public void dialogueTerminated(DialogueStats stats);
	
}
