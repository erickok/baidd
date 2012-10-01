package org.aspic.inference;

/**
 * An ElementList whose members are restricted to Constants.  
 * Used for Query expressions.
 * @author mjs (matthew.south @ cancer.org.uk)
 *
 */
public class ConstantList extends ElementList {
	
	/**
	 * Default Constructor.
	 */
	public ConstantList() {
		
	}
	/**
	 * Typical constructor. Allows variable number of Constants to be added at construction.
	 * @param constants var-args list of Constants.
	 */
	public ConstantList(Constant... constants) { 
		for (Constant constant: constants) {	
			super.add(constant);
		}
	}
	
	/**
	 * Add new Constant to end of ConstantList.
	 * @param constant Constant to add
	 * @return true if list was changed
	 */
	public boolean add(Constant constant) {
		super.add(constant);
		return true;
	}
	
	/**
	 * Add new Constant to specified index in ConstantList (pushes higher Constants up).
	 * @param index specified index
	 * @param constant Constant to add.
	 */
	public void add(int index, Constant constant) {
		super.add(index, constant);	
	}
	
	/**
	 * Add new Constant to specified index in ConstantList (overwrites existing Constant).
	 * @param index specified index.
	 * @param constant Constant to add.
	 * @return true if list was changed
	 */
	public Constant set(int index, Constant constant) {
		return (Constant) super.set(index, constant);
	}
	
	/**
	 * Get Constant at specified index.
	 * @param index specified index
	 * @return Constant at specified index
	 */
	public Constant get(int index) {
		return (Constant) super.get(index);
	}
	
	/**
	 * Remove Constant at specified index.
	 * @param index specified index.
	 * @return removed Constant.
	 */
	public Constant remove(int index) {
		return (Constant) super.remove(index);
	}
}
