package org.aspic.inference;

/**
 * This exception should be raised if the reasoner fails
 * 
 * @author mjs (matthew.south @ cancer.org.uk)
 */
public class ReasonerException extends Exception {
	  public ReasonerException(String msg) { super(msg); }
	  public ReasonerException(String msg, Throwable cause) { super(msg, cause); }
}
