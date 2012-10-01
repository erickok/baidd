package org.aspic.inference;

/**
 * A Predicate provides a meta-view of a Constant and is used in 
 * the {@link KnowledgeBase} class to represent it's contents.
 * For example, <li><code>likes(sally, george)</code> has predicate: <code>likes/2</code>,
 * where "likes" is the functor and 2 is the arity.
 * 
 * @author mjs (matthew.south @ cancer.org.uk)
 *
 */
public class Predicate {
	private String functor=null;
	private int arity;
	/**
	 * Getter for arity.
	 * @return predicate's arity.
	 */
	public int getArity() {
		return arity;
	}
	/** 
	 * Setter for arity.
	 * @param arity predicate's arity.
	 */
	public void setArity(int arity) {
		this.arity = arity;
	}
	/**
	 * Getter for functor.
	 * @return predicate's functor.
	 */
	public String getFunctor() {
		return functor;
	}
	/**
	 * Setter for functor.
	 * @param functor predicate's functor.
	 */
	public void setFunctor(String functor) {
		this.functor = functor;
	}
	/**
	 * Default constructor.
	 */
	public Predicate() {}
	/**
	 * Typical constructor.
	 * @param functor predicate's functor.
	 * @param arity predicate's arity.
	 */
	public Predicate(String functor, int arity) {
		super();
		this.functor = functor;
		this.arity = arity;
	}
	
	public boolean equals(Object o) {
		return ((o instanceof Predicate) && ((Predicate) o).getFunctor().equals(this.functor) && ((Predicate) o).getArity()==this.arity); 
	}
}
