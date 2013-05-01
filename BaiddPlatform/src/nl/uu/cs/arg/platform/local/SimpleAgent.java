package nl.uu.cs.arg.platform.local;

import java.util.List;

import nl.uu.cs.arg.shared.dialogue.DialogueException;
import nl.uu.cs.arg.shared.dialogue.Move;
import nl.uu.cs.arg.shared.dialogue.locutions.Locution;

import org.aspic.inference.Constant;
import org.aspic.inference.ReasonerException;
import org.aspic.inference.parser.ParseException;

public class SimpleAgent extends DeliberatingAgent {

	public SimpleAgent(AgentXmlData xmlDataFile) {
		super(xmlDataFile);
	}

	@Override
	protected void storeNewBeliefs(List<Move<? extends Locution>> moves) throws ParseException, ReasonerException {
	}

	@Override
	protected List<ValuedOption> evaluateAllOptions(List<Constant> options) throws ParseException, ReasonerException {
		return null;
	}

	@Override
	protected void analyseOptions(List<ValuedOption> valuedOptions) {
	}

	@Override
	protected List<Move<? extends Locution>> generateMoves(List<ValuedOption> valuedOptions) throws DialogueException, ParseException, ReasonerException {
		return null;
	}

}
