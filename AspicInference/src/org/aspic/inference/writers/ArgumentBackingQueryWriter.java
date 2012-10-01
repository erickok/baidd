package org.aspic.inference.writers;

import java.util.Iterator;

import org.aspic.inference.Constant;
import org.aspic.inference.ElementList;
import org.aspic.inference.KnowledgeBase;
import org.aspic.inference.Query;
import org.aspic.inference.ReasonerPair;
import org.aspic.inference.Rule;
import org.aspic.inference.RuleArgument;
import org.aspic.inference.RuleArgumentList;
import org.aspic.inference.Substitution;
import org.aspic.inference.Term;
import org.aspic.inference.Variable;

/**
 * A writer that writes the backing of an argument as an I.E. query.
 * e.g.
 * viable(D, O, R) since d(D, O), r(O, R), [vs(D, O, R)] viable(D, O, R) <- d(D, O), r(R, O).
 * would write:
 * d(D, O), r(O, R), vs(D, O, R).
 * 
 * @author mjs
 *
 */
public class ArgumentBackingQueryWriter implements ReasonerWriter, KnowledgeWriter {

	StringBuffer buffer = new StringBuffer();
	
	public void write(Query query) {
		// do nothing
	}

	public void write(RuleArgument argument) {
		// only write sub arguments.
		write(argument.getSubArgumentList());
	}	

	public void write(RuleArgumentList argumentList) {
		Iterator<RuleArgument> itr = argumentList.getArguments().iterator();
		while (itr.hasNext()) {
			RuleArgument argument = itr.next();
			buffer.append(argument.getClaim());
			if (itr.hasNext()) buffer.append(", ");
		}
		buffer.append(".");
	}

	public void write(Substitution substitution) {
		// do nothing
		
	}

	public void write(ReasonerPair reasonerPair) {
		// do nothing
	}

	public void write(Constant constant) {
		// do nothing
	}

	public void write(ElementList elementList) {
		// do nothing
	}

	public void write(Rule rule) {
		// do nothing
	}

	public void write(Term term) {
		buffer.append(term.toString());
	}

	public void write(Variable variable) {
		buffer.append(variable.toString());
	}

	public void write(KnowledgeBase kb) {
		// do nothing
	}
	
	public String toString() {
		return buffer.toString();
	}

	public void clear() {
		buffer = new StringBuffer();
	}
}
