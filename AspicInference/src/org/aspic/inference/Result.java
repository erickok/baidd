package org.aspic.inference;

import java.io.Serializable;



/**
 * Simple data class for representing a match to a <code>Query</code> expression.
 * Accessible via the Query.getResults() collection.
 * @author mjs (matthew.south @ cancer.org.uk)
 */
public class Result implements Serializable {
	private Element expression = null;
	private boolean result = false;
	/**
	 * Default constructor.
	public Result() {
	}
	 */
	/**
	 * Typical constructor.
	 * @param expression result's expression.
	 * @param result - is this ArgumentList undefeated or not
	 */
	Result(Element expression, boolean result) {
		this.expression = expression;
		this.result = result;
	}
	/**
	 * Getter for expression.
	 * @return expression
	 */
	public Element getExpression() {
		return expression;
	}
	/**
	 * Getter for result.
	 * @return result - is this ArgumentList undefeated or not.
	 */
	public boolean isUndefeated() {
		return result;
	}
	/**
	 * Setter for expression.
	 * @param expression result's expression.
	 */
	void setExpression(Element expression) {
		this.expression = expression;
	}
	/**
	 * Setter for result.
	 * @param undefeated result - is this ArgumentList undefeated or not.
	 */
	void setUndefeated(boolean undefeated) {
		this.result = undefeated;
	}
	
	public String toString() {
		return expression.toString() + ". " + (result ? "yes" : "no");
	}
}