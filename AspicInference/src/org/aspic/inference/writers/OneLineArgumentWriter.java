package org.aspic.inference.writers;

import java.util.Iterator;

import org.aspic.inference.Query;
import org.aspic.inference.ReasonerPair;
import org.aspic.inference.RuleArgument;
import org.aspic.inference.RuleArgumentList;
import org.aspic.inference.Substitution;

/**
 * A ReasonerWriter that writes arguments on a single line like this:
 * 
 * [Arg5 : fly(tweety) 0.5 <- [Arg3 : bird(tweety) 0.5 <- Arg1 : penguin(tweety) 0.5, Arg2 : [r2] bird(X) <- penguin(X) 0.5], Arg4 : [r1] fly(X) <- bird(X) 0.5]
 *  
 * @author mjs
 *
 */
public class OneLineArgumentWriter implements ReasonerWriter {
	StringBuffer buffer;
	
	public void write(Query query) {
		// TODO Auto-generated method stub

	}

	public void write(RuleArgument argument) {
		clear();
		writeArgument(argument);
	}

	public void write(RuleArgumentList argumentList) {
		Iterator<RuleArgument> itr = argumentList.getArguments().iterator();
		while (itr.hasNext()) {
			writeArgument(itr.next());
			if (itr.hasNext()) buffer.append(", ");
		}
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

	private void writeArgument(RuleArgument argument) {
		if (argument.getSubArgumentList().getArguments().size()>0) 
				buffer.append("[");
		buffer.append(argument.getName());
		buffer.append(" : ");
		if (argument.getClaim().getKnowledgeBase().isRuleName(argument.getClaim())) {
			buffer.append(argument.getClaim().getKnowledgeBase().getRuleFromName(argument.getClaim()).toString());
		} else {
			buffer.append(argument.getClaim().toString());			
			buffer.append(" ");
			buffer.append(argument.getModifier());
		}
		if (argument.getSubArgumentList().getArguments().size()>0) {
			buffer.append(" <- ");
			write(argument.getSubArgumentList());
			buffer.append("]");		
		}
	}
}
