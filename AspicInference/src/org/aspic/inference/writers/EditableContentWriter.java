package org.aspic.inference.writers;

import java.util.Iterator;

import org.aspic.inference.Constant;
import org.aspic.inference.Element;
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
 * A writer that starts a user off for an editable move content.
 * 
 * @author mjs
 *
 */
public class EditableContentWriter implements ReasonerWriter, KnowledgeWriter {

	StringBuffer buffer = new StringBuffer();
	
	public void write(Query query) {
		// do nothing
	}

	public void write(RuleArgument argument) {
		write1(argument);
		buffer.append("\n");
		if (argument.getSubArgumentList().getArguments().size()==0) {
			write1(argument);			
		} else {
			write(argument.getSubArgumentList());
		}
	}	

	public void write(RuleArgumentList argumentList) {
		Iterator<RuleArgument> itr = argumentList.getArguments().iterator();
		while (itr.hasNext()) {
			RuleArgument argument = itr.next();
			write1(argument);
			if (argument.getSubArgumentList().getArguments().size()>0) {
				buffer.append("\n");
				write(argument.getSubArgumentList());
			}
			if (itr.hasNext()) buffer.append("\n");
		}
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
	
	private void write1(RuleArgument argument) {
		Constant claim = argument.getClaim();
		if (claim.getKnowledgeBase().isRuleName(claim)) {
			Rule rule = claim.getKnowledgeBase().getRuleFromName(claim);
			buffer.append("[");
			buffer.append(rule.getName().toString());
			buffer.append("] ");
			buffer.append(rule.getConsequent().toString());
			buffer.append(" <-\n");
			Iterator<Element> iterator = rule.getAntecedent().iterator();
			while (iterator.hasNext()) {
				buffer.append("\t");
				buffer.append(iterator.next().toString());
				buffer.append(iterator.hasNext() ? ",\n" : (rule.getDob()==1.0 ? "" : rule.getDob()) + ".");
			}
		} else {
			buffer.append(claim.toString());
			buffer.append(argument.getModifier()<1.0 ? " " + argument.getModifier() : "");
			buffer.append(".");
		}
	}
}
