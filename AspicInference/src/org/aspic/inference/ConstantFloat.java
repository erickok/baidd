package org.aspic.inference;


/**
  * The knowledge syntax is 
  * based on prolog which understands that numbers might be 
  * integers or floats.  This class represents a float, which
  * is implemented as a Java double.
  * 
  * @author mjs (matthew.south @ cancer.org.uk)
  *
  */
public class ConstantFloat extends ConstantNumber {
	
	/**
	 * Typical constructor.
	 * @param number ConstantFloat value.
	 */
	public ConstantFloat(double number) {
		super(new Double(number));
	}
	
	/**
	 * Default constructor.
	 */
	public ConstantFloat() {}
	
	/**
	 * Simple setter for number.
	 * @param number ConstantFloat value
	 */
	public void setNumber(double number) {
		super.setNumber(new Double(number));
	}

	/**
	 * Simple getter for number.
	 * @return ConstantFloat value
	 */
	public Double getNumber() {
		return new Double(super.getNumber().doubleValue());
	}
	
	public Object clone() throws CloneNotSupportedException {
		ConstantFloat result = (ConstantFloat) super.clone();
		result.setNumber(new Double(result.getNumber()));
		return result;
	}
}
