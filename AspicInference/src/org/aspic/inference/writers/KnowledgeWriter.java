package org.aspic.inference.writers;

import org.aspic.inference.*;

/** 
 * A writer interface for all non abstract knowledge classes.
 * 
 * Note: this might be overkill.  The inspect() methods will suffice until we have a situation where we have more than one syntax.
 * 
 * @author mjs (matthew.south @ cancer.org.uk)
 */
public interface KnowledgeWriter {
	void write(Constant constant);
	void write(ElementList elementList);
	void write(Rule rule);
	void write(Term term);
	void write(Variable variable);
	void write(KnowledgeBase kb);
}
