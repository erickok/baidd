package org.aspic.inference;


/**
 * The knowledge syntax is 
 * based on prolog which understands that numbers might be 
 * integers or floats.  This class represents an integer.
 * 
 * @author mjs (matthew.south @ cancer.org.uk)
 *
 */
public class ConstantInteger extends ConstantNumber {
	/**
	 * Typical constructor.
	 * @param number ConstantInteger value
	 */
	public ConstantInteger(int number) {
		super(new Integer(number));
	}
	
	/**
	 * Default constructor.
	 */
	public ConstantInteger() {}
	
	/**
	 * Setter for number.
	 * @param number ConstantInteger value
	 */
	public void setNumber(int number) {
		super.setNumber(new Integer(number));
	}

	/**
	 * Getter for number.
	 * @return ConstantInteger value
	 */
	public Integer getNumber() {
		return new Integer(super.getNumber().intValue());
	}
	
	public Object clone() throws CloneNotSupportedException {
		ConstantInteger result = (ConstantInteger) super.clone();
		result.setNumber(new Integer(result.getNumber()));
		return result;
	}

}
