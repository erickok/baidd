package org.aspic.inference.writers;

import java.io.IOException;
import java.io.Writer;

import org.aspic.inference.*;

/**
 * A writer that outputs the PrologSyntax input.
 * This writer allows you to capture, as near as possible
 * the input that was entered to the PrologSyntax parser
 * to generate a particular KnowledgeBase.  By default
 * it hides automatically generated rule names and automatically
 * generated rule hook terms, e.g. if your KB consists of 
 * simply the rule 
 * <pre>a &lt;- b, c 0.4.</pre>
 * then, while entering the rule
 * to the kb it will give it a name, <pre>r1</pre> and create a standalone
 * term, <pre>r1 0.4.</pre>, the rule hook.  So if you set <code>showAutoGeneratedRuleNames</code>
 * and <code>showRuleHooks</code> to true then the this writer would output
 * the following for our apparently one ruled knowledge base:
 * <pre>r1: a <- b, c 0.4.
 * r1 0.4.</pre>
 * Actually, it has two expressions in it.  The "near as possible"
 * caveat was added because the order of your rules cannot be
 * guarenteed.
 * <p>
 * TODO?: Capture order of entered Rules and sort the output here <br/>
 * </p>
 * @author mjs (matthew.south @ cancer.org.uk)
 *
 */
public class PrologSyntaxKnowledgeWriter implements KnowledgeWriter {
	/*
	 * Why embed a java.io.Writer? So that you can pass in a StringWriter or
	 * a FileWriter, or a PipedWriter or System.out.  The idea is that
	 * it gives you some flexibility.
	 * Examples of usage can be found in TestWriters
	 */
	protected Writer outstream;
	
	public boolean showAutoGeneratedRuleNames=false;
	public boolean showRuleHooks=false;

	public PrologSyntaxKnowledgeWriter(Writer outstream) {
		this.outstream = outstream;
	}

	public PrologSyntaxKnowledgeWriter(Writer outstream, boolean showAutoGeneratedRuleNames, boolean showRuleHooks, 
			boolean showRuleNamesForFacts) {
		this.outstream = outstream;
		this.showAutoGeneratedRuleNames = showAutoGeneratedRuleNames;
		this.showRuleHooks = showRuleHooks;
	}

	public void write(Constant constant) {
		appendCautiously(constant.inspect());
	}

	public void write(ElementList elementList) {
		appendCautiously(elementList.inspect());
	}

	public void write(Rule rule) {		
		String temp = rule.inspect(showAutoGeneratedRuleNames, showRuleHooks);
		if (temp.length()>0) {
			appendCautiously(temp);
			appendCautiously(".\n");
		}
	}

	public void write(Term term) {
		appendCautiously(term.inspect());
	}

	public void write(Variable variable) {
		appendCautiously(variable.inspect());
	}

	public void write(KnowledgeBase kb) {
		appendCautiously(kb.inspect(showAutoGeneratedRuleNames, showRuleHooks));
	}
	
	/** 
	 * Helper function to trap errors (needed for a Writer, as opposed to a PrintWriter)
	 * 
	 * @param string to append to outstream
	 */
	private void appendCautiously(String string) {
		try {
			outstream.append(string);
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
}
