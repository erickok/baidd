package org.aspic.inference;

import java.util.Iterator;




/**
 * A generic Argument Iterator that yields no Arguments.
 * 
 * @author mjs (matthew.south @ cancer.our.uk)
 */
class EmptyArgumentIterator implements Iterator<RuleArgument> {		
	public EmptyArgumentIterator() {}		
	public boolean hasNext() { return false; }		
	public RuleArgument next() { return null; }		
	public void remove() {
		throw new UnsupportedOperationException();			
	}
}
