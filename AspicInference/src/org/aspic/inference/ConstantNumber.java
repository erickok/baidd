package org.aspic.inference;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Logger;


/**
 * A <code>ConstantNumber</code> is a special <code>Constant</code> that you can always make exactly one
 * <code>Argument</code> for.
 * 
 * @author mjs (matthew.south @ cancer.org.uk)
 *
 */
public abstract class ConstantNumber extends Constant {
	private static Logger logger = Logger.getLogger(Constant.class.getName());		
	private Number number;
	
	/**
	 * Default constructor.
	 */
	public ConstantNumber() {}
	
	/**
	 * Typical constructor.
	 * @param number ConstantNumber value.
	 */
	public ConstantNumber(Number number) {
		super(number.toString());
		this.number = number;
	}
	
	/**
	 * Basic getter for number.
	 * @return ConstantNumber value
	 */
	public Number getNumber() {
		return number;
	}
	
	/**
	 * Basic Setter for number.
	 * @param number ConstantNumber value
	 */
	public void setNumber(Number number) {
		this.number = number;
		setFunctor(number.toString());
	}
	
	/**
	 * Not supported.
	 * @throws UnsupportedOperationException
	 */
	public Constant negation() {
		throw new UnsupportedOperationException();			
	}
	
	/**
	 * Add this number to the passed number.  Returns ConstantInteger if both operands are ConstantIntegers
	 * else returns ConstantFloat.
	 * @param constantNumber number to add to this one.
	 * @return new ConstantNumber with  result of the addition operation.
	 */
	ConstantNumber add(ConstantNumber constantNumber) {
		ConstantNumber result;
		if (this instanceof ConstantInteger && constantNumber instanceof ConstantInteger) {
			result = new ConstantInteger(this.number.intValue() + constantNumber.number.intValue());
		} else {
			result = new ConstantFloat(this.number.doubleValue() + constantNumber.number.doubleValue());
		}
		result.setKnowledgeBase(this.getKnowledgeBase());
		return result;
	}
	
	/**
	 * Subtract the passed number from this number.  Returns ConstantInteger if both operands are ConstantIntegers
	 * else returns ConstantFloat.
	 * @param constantNumber number to take away from this one.
	 * @return new ConstantNumber with result of the substraction operation.
	 */
	ConstantNumber subtract(ConstantNumber constantNumber) {
		ConstantNumber result;
		if (this instanceof ConstantInteger && constantNumber instanceof ConstantInteger) {
			result = new ConstantInteger(this.number.intValue() - constantNumber.number.intValue());
		} else {
			result = new ConstantFloat(this.number.doubleValue() - constantNumber.number.doubleValue());
		}
		result.setKnowledgeBase(this.getKnowledgeBase());
		return result;
	}
	
	/**
	 * Multiply this number and the passed number.  Returns ConstantInteger if both operands are ConstantIntegers
	 * else returns ConstantFloat.
	 * @param constantNumber number to take away from this one.
	 * @return new ConstantNumber with result of the multiplication operation.
	 */
	ConstantNumber multiply(ConstantNumber constantNumber) {
		ConstantNumber result;
		if (this instanceof ConstantInteger && constantNumber instanceof ConstantInteger) {
			result = new ConstantInteger(this.number.intValue() * constantNumber.number.intValue());
		} else if (this instanceof ConstantFloat && constantNumber instanceof ConstantFloat) {
			result = new ConstantFloat(this.number.doubleValue() * constantNumber.number.doubleValue());
		} else {
			Number value;
			if (this instanceof ConstantFloat) {
				value = this.number.floatValue() * constantNumber.number.intValue();
			} else {
				value = this.number.intValue() * constantNumber.number.floatValue();				
			}
			if (value.intValue()==value.doubleValue()) {
				result = new ConstantInteger(value.intValue());
			} else {
				result = new ConstantFloat(value.doubleValue());
			}
		}
		result.setKnowledgeBase(this.getKnowledgeBase());
		return result;
	}
	
	/**
	 * Divide this number by the passed value.  Returns ConstantFloat.
	 * @param constantNumber number to divide this one.
	 * @return new ConstantNumber with result of the division operation.
	 */
	ConstantNumber divide(ConstantNumber constantNumber) {
		ConstantNumber result;
		result = new ConstantFloat(this.number.doubleValue() / constantNumber.number.doubleValue());
		result.setKnowledgeBase(this.getKnowledgeBase());
		return result;
	}
	
	/**
	 * Provide an iterator for the one argument that can be generated from a Number.
	 **/
	Iterator<RuleArgument> argumentIterator(Double needed, Party party, int level, int d_top, RuleArgumentValuator valuator, boolean restrictedRebutting) {
		return new NumberArgumentIterator(needed, party, level, d_top, valuator, restrictedRebutting);
	}
	
	/**
	 * 
	 * @author mjs (matthew.south @ cancer.org.uk)
	 *
	 */
	private class NumberArgumentIterator implements Iterator<RuleArgument> {
		
		private RuleArgument nextArgument;
		private boolean queuedArgument;
		
		public NumberArgumentIterator(Double needed, Party party, int level, int d_top, RuleArgumentValuator valuator, boolean restrictedRebutting) {
			logger.fine(party.toString() + ": searching for arguments for literal " + ConstantNumber.this.inspect());
			Rule topRule = new Rule(ConstantNumber.this);
			topRule.setKnowledgeBase(ConstantNumber.this.getKnowledgeBase());
			nextArgument = new RuleArgument(topRule, new Double(1.0), new Substitution(), new RuleArgumentList(), party, level, d_top, valuator, restrictedRebutting);
			queuedArgument=true;
		}
		
		public boolean hasNext() {
			return queuedArgument;
		}
		
		public RuleArgument next() {
			if (queuedArgument) {
				queuedArgument=false;
				return nextArgument;
			} else {
				throw new NoSuchElementException();
			}		
		}
		
		public void remove() {
			throw new UnsupportedOperationException();			
		}
	}
	
	public boolean equals(Object test) {
		return (test instanceof ConstantNumber && this.getNumber().equals(((ConstantNumber) test).getNumber()));
	}

	List<Predicate> getPredicates() {
		return new ArrayList<Predicate>();
	}
}
