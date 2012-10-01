package org.aspic.inference.writers;

import org.aspic.inference.Constant;
import org.aspic.inference.Query;
import org.aspic.inference.ReasonerPair;
import org.aspic.inference.Rule;
import org.aspic.inference.RuleArgument;
import org.aspic.inference.RuleArgumentList;
import org.aspic.inference.Substitution;

/**
 * A writer that writes an argument as a tab indented tree of sub argument captions
 * 
 * @author mjs
 *
 */
public class ArgumentCaptionWriter implements ReasonerWriter {
	
	StringBuffer buffer;
	
	public void write(Query query) {
		clear();
		buffer.append(query.getExpression().toString());
		buffer.append("\n");
		for (RuleArgument argument : query.getProof()) {
			write(argument);
		}
	}

	public void write(RuleArgument argument) {
		clear();
		writearg(argument, 0);
	}

	public void write(RuleArgumentList argumentList) {
		clear();
		writeArgumentList(argumentList, 0);
	}

	public void write(Substitution substitution) {
		// TODO Auto-generated method stub

	}

	public void write(ReasonerPair reasonerPair) {
		// TODO Auto-generated method stub

	}
	
	public void clear() {
		buffer = new StringBuffer();
	}
	
	public String toString() {
		return buffer.toString();
	}
	
	private void writearg(RuleArgument argument, int indentation) {
		indent(indentation);
		Rule topRule;
		Constant claim = argument.getClaim();
		if (claim.getKnowledgeBase().isRuleName(claim)) {
			topRule = claim.getKnowledgeBase().getRuleFromName(claim);
		} else {
			topRule = argument.getTopRule();
		}
		buffer.append(topRule.getCaption());
		if (topRule.getDescription()!=null) {
			buffer.append(" (");
			buffer.append(topRule.getDescription());
			buffer.append(") ");
		}
		buffer.append("\n");
		writeArgumentList(argument.getSubArgumentList(), indentation+1);
	}
	
	private void writeArgumentList(RuleArgumentList argumentList, int indentation) {
		for (RuleArgument argument : argumentList.getArguments()) {
			writearg(argument, indentation);
		}
	}
	
	private void indent(int indentation) {
		for (int i=0; i<indentation; i++) {
			buffer.append("\t");
		}
	}
}
