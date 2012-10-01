package org.aspic.inference;

import java.io.Serializable;
import java.util.List;

import org.aspic.inference.writers.KnowledgeWriter;


/**
 * Top-level abstract class for all knowledge classes.
 * Allows all knowledge classes to be unified.
 * @author mjs (matthew.south @ cancer.org.uk)
 *
 */
public abstract class Element implements Cloneable, Serializable {

	/**
	 * A reference to the knowledgeBase that contains this Element.
	 */
	private KnowledgeBase knowledgeBase;
    private long id = -1;
    
    /*
    public abstract String toStringXml();
    */
	
	/**
	 * (idea from the Ruby prototype)
	 * inspect allows us to see a representation of the
	 * text expression that the object is based on.
	 * Not implemented by overriding toString() because it's 
	 * still useful to be able to inspect the objectID which 
	 * toString returns.
	 * @return String representation of this knowledge atom
	 */
	public abstract String inspect();
	
	// TODO?: rename to cloneAndSubstitute
	/**
	 * Return copy of this Element with the substitution applied.
	 * @param subs Substitution to apply
	 * @return Copy of this Element with substitution applied to it.
	 */
	public abstract Element apply(Substitution subs);
	
	/**
	 * Return true if this Element can be unified with the passed Element.
	 * @param element
	 * @return true iff testClause can be unified with the passed clause
	 */
	public abstract boolean isUnifiable(Element element);	
	
	/**
	 * Try to unify this Element with another Element, using passed substitution as a starting point.
	 * If the clause is not unifiable (@see isUnifiable), subs is returned unaffected.   
	 * @param element Element to be unified with this Element
	 * @param subs starting substitution
	 * @return Most General Unifier (MGU) as a Substitution object
	 */
	public abstract Substitution unify(Element element, Substitution subs);
	
	/**
	 * Check to see if this Element matches another, bar Variable names
	 * i.e. likes(john, X) isEqualModuloVariables to likes(john, Y).
	 * @param element Element to checked against this one.
	 * @return true iff testClause is the same as this Element, apart from Variable names.
	 */
	abstract boolean isEqualModuloVariables(Element element);
	
	/**
	 * Check for free variables
	 * @return true iff this Element has no free variables
	 */
	abstract boolean isGrounded();
	
	/*
	 * argument_tag is used to cache arguments for a given constant
	 * Note: This isnt used. It was implemented in Ruby prototype as a performance enhancement.
	 * @param needed
	 * @return
	 */
	//public abstract String argumentTag(Double needed);
	
	/**
	 * When a rule is added to a knowledgeBase this method should be called on 
	 * all the rule's sub-elements so that it's properly integrated into the kb.
	 * @param kb
	 */
	public void setKnowledgeBase(KnowledgeBase kb) {
		this.knowledgeBase = kb;
	}

	/**
	 * Get reference to the kb that this Element is associated with.
	 * @return kb reference to knowledge base this Element is attached to.
	 */
	public KnowledgeBase getKnowledgeBase() {
		return this.knowledgeBase;
	}

	/** 
	 * Using the Visitor pattern, this method allows the details
	 * of an Element to be written in different ways.
	 * @param writer the object that does the writing.
	 */
	public abstract void write(KnowledgeWriter writer);
	
	public String toString() {
		return this.inspect();
	}
	
	/**
	 * Get a List of all variables contained within this Element.
	 * 
	 * @return List of all Variables.  If no variables are contained within, return an empty list.
	 */
	abstract List<Variable> getVariables();

	/**
	 * Get a List of all Functor/Arity combinations contained within this Element.
	 * 
	 * @return List of all Predicates.
	 */
	abstract List<Predicate> getPredicates();

	/**
	 * An element is incompletely cloned.  Everything except it's knowledgeBase
	 * reference is cloned.
	 */
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
	
	/**
	 * Getter for Persistance ID.
	 * @return long ID assigned by Persistance layer (if used).
	 */
    public long getId() {
        return id;
    }
    
    /**
     * Setter for Persistance ID.
     * @param id unique id assigned by Persistance layer.
     */
    public void setId(long id) {
        this.id = id;
    }
}
