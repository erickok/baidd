package org.aspic.inference.writers;

import java.io.IOException;
import java.io.Writer;

import org.aspic.inference.KnowledgeBase;

/**
 * This Writer is a variation of PrologSyntaxKnowledgeWriter, 
 * which it extends by overwriting the write(KnowledgeBase) 
 * method.  It shows a lower level view of 
 * the KnowledgeBase's rules index which is useful for debugging.
 * 
 * @author mjs (matthew.south @ cancer.org.uk)
 */
public class RawKnowledgeWriter extends PrologSyntaxKnowledgeWriter {
	public RawKnowledgeWriter(Writer outstream) {
		super(outstream);
	}
	public void write(KnowledgeBase kb) {
		appendCautiously(kb.toString());
	}
	/** 
	 * Helper function to trap errors
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
