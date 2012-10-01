package org.aspic.inference;

import java.util.Iterator;



/** 
 * A generic ArgumentIterator class that yields one Argument, as supplied in the constructor.
 * 
 * @author mjs (matthew.south @ cancer.org.uk)
 */
class SingleArgumentIterator implements Iterator<RuleArgument> {
	private RuleArgument queuedArgument;
	private boolean isQueued;
	
	public SingleArgumentIterator(RuleArgument argument) {
		queuedArgument = argument;
		isQueued=true;
	}
	
	public boolean hasNext() {
		return isQueued;
	}
	
	public RuleArgument next() {
		if (isQueued==true) {
			isQueued=false;
			return queuedArgument;
		} else {
			return null;
		}		
	}
	
	public void remove() {
		throw new UnsupportedOperationException();			
	}
}
