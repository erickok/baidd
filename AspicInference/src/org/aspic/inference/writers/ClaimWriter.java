package org.aspic.inference.writers;

import java.util.Iterator;

import org.aspic.inference.*;

/**
 * A writer for Arugment claims.  Status: Prototype.
 * 
 * @author mjs (matthew.south @ cancer.org.uk)
 *
 */
public class ClaimWriter implements ReasonerWriter {
	private StringBuffer text = new StringBuffer();
	
	public void write(Query query) {
		// do nothing
	}

	public void write(RuleArgument argument) {
		text.append(argument.getClaim().toString());
	}

	public void write(RuleArgumentList argumentList) {
		Iterator<RuleArgument> iterator = argumentList.getArguments().iterator();
		while(iterator.hasNext()) {
			RuleArgument arg = iterator.next();
			arg.write(this);
			if (iterator.hasNext()) text.append(", ");
		}
	}

	public void write(Substitution substitution) {
		// do nothing
	}

	public void write(ReasonerPair reasonerPair) {
		// do nothing
	}
	
	public void reset() {
		text = new StringBuffer();
	}
	
	public String toString() {
		return text.toString();
	}

}
