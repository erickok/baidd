package org.aspic.inference;

import java.util.*;

import org.aspic.inference.writers.KnowledgeWriter;

/**
 * <p>
 * A Term uses the inherited Constant's functor as it's predicate name and adds an additional list of arguments, <code>argList</code>. 
 * The number of arguments that a particular Term has is called it's arity.  Thus the term, <code>likes(john, sally)</code>
 * can be written abstractly as likes/2 - the predicate, "likes" with an arity of 2.  Using this nomenclature, a constant 
 * can be thought of as a predicate with arity 0.</p>
 * <p>Note the use of the word "argument" in this context. A Term has arguments, i.e. in the relation: 
 * "parent(george, sally)", which is a way of saying "george is the parent 
 * of sally", the arguments are "george" and "sally".  
 * These are *not* the same as <code>Argument</code>s that are developed by a reasoner.
 * </p>
 * 
 * @author mjs (matthew.south @ cancer.org.uk)
 */
public class Term extends Constant {
	
	private ElementList argList = new ElementList();

	/**
	 * Default Constructor.
	 */
	public Term() {		
	}
    

    public void setArgList(ElementList argList) {
        this.argList = argList;
    }  
        
	
    /*
    public String toStringXml(){
        StringBuffer result = new StringBuffer("<term>" + 
                                               "<functor>" + getFunctor() + "</functor>" + 
                                               getArgList().toStringXml() + 
                                               "</term>");
        return result.toString();
    }
     */  
    
	/**
	 * Typical Constructor.
	 * @param functor the term's functor
	 * @param argList the term's arguments
	 */
	public Term(String functor, ElementList argList) {
		super(functor);
		this.argList = argList;
		consolidateVariables();
	}
	
	/**
	 * Constructor using Java 5 varargs, mainly for internal use (via Rule) 
	 * @param functor the term's functor
	 * @param arguments the term's arguments
	 */
	public Term(String functor, Element... arguments) {
		super(functor);
		for (Element argument: arguments) {
			this.argList.add(argument);
		}
		consolidateVariables();
	}
	
	/**
	 * Adds new element to argList in the stated index position.  
	 * Subsequent arguments are shifted along.
	 * @param index position to add this element
	 * @param element element to add to this term's argList
	 * @return reference to (enhanced) Term.
	 */
	public Term addArg(int index, Element element) {
		argList.add(index, element); 
		consolidateVariables();
		return this; 
	}
	
	/**
	 * Retrieve argList element by index.
	 * @param index index of element to retrieve (0 <=index < size)
	 * @return requested element
	 */
	public Element getArg(int index) {
		return argList.get(index);
	}
	
	ElementList getArgList() {
		return argList;
	}
	
	/**
	 * Overwrite's an existing position in the Term's argList.
	 * @param index position to set this element
	 * @param element element to overwrite with
	 * @return reference to (enhanced) Term.
	 */
	public Term setArg(int index, Element element) {
		argList.set(index, element);
		consolidateVariables();
		return this;
	}
	
	/**
	 * Retrieve size of argList.
	 * @return size of argList.
	 */
	public int numberOfArgs() {
		return argList.size();
	}
	
	/**
	 * Clone argList and apply substitution to all elements within the cloned list.
	 * @param subs substitution to apply
	 * @return cloned and substituted argList
	 */
	ElementList applyArgs(Substitution subs) {
		// TODO: why can't Rule.apply just inherit this (I've tried it and lot's falls over)?
		return argList.apply(subs);
	}
	
	public String inspect() {
		if (this.getFunctor().equals("~")) {
			return super.inspect() + argList.inspect();
		} else {
			return super.inspect() + "(" + argList.inspect() + ")";
		}
	}
	
	public Constant apply(Substitution subs) {
		if (this.isGrounded()) { 
			return this; 
		} else {
			Term newTerm = new Term(this.getFunctor(), argList.apply(subs));
			newTerm.setKnowledgeBase(this.getKnowledgeBase());
			return newTerm;
		}
	}
	
	public boolean isUnifiable(Element toUnify) {
		return ((toUnify instanceof Variable) ||
					(	
						(toUnify instanceof Term) && 
						(((Term) toUnify).getFunctor().equals(this.getFunctor())) &&
						((Term) toUnify).getArgList().isUnifiable(this.argList)
					)
				);
	}
	
	public Substitution unify(Element toUnify, Substitution subs) {
		if (toUnify instanceof Variable) {
			subs.add((Variable) toUnify, this);
			return subs;
		} else if (toUnify instanceof Term) {
			if (((Term) toUnify).getFunctor().equals(this.getFunctor())) {
				return argList.unify(((Term) toUnify).getArgList(), subs);
			}
		}
		return subs;	
	}

	public boolean isEqualModuloVariables(Element testClause) {
		return (testClause instanceof Term) &&
			(((Term)testClause).getFunctor().equals(this.getFunctor()))
			&& (((Term)testClause).getArgList().isEqualModuloVariables(this.argList));
	}
	
	public boolean isGrounded() {
		return argList.isGrounded();
	}
	
	public String ruleTag() {
		return this.getFunctor().equals("~") ? "neg_" + ((Constant) argList.get(0)).getFunctor() : this.getFunctor() ;
	}

	public void write(KnowledgeWriter writer) {
		writer.write(this);
	}
	
	/*
	public String argumentTag(Double needed) {
		String result = functor + "(";
		Iterator<Element> itr = argList.iterator();
		while (itr.hasNext()) {
			Element next = itr.next();
			result += next.argumentTag(needed);
			if (itr.hasNext()) result += ", ";
		}
		return result + ")_" + needed.toString();
	}
	*/
	
	public void setKnowledgeBase(KnowledgeBase kb) {
		//this.knowledgeBase = kb;
		super.setKnowledgeBase(kb);
		argList.setKnowledgeBase(kb);
	}
	
	public Constant negation() {
		if (this.getFunctor().equals("~")) {
			return (Constant) argList.get(0);
		} else {
			Term negation = new Term("~", this);
			negation.setKnowledgeBase(this.getKnowledgeBase());
			return negation;
		}
	}
	
	List<Variable> getVariables() {
		return argList.getVariables();
	}
	
	public boolean equals(Object o) {
		return (o instanceof Term) && (this.getFunctor().equals(((Term) o).getFunctor())) && (this.getArgList().equals(((Term) o).getArgList()));
	}
	
	public int hashCode() {
		int result = 17;
		result = 37*result + this.getFunctor().hashCode();
		result = 37*result + this.getArgList().hashCode();
		return result;
	}
	
	public Object clone() throws CloneNotSupportedException {
		Object result = super.clone();
        //Henrik: 02/11/06 - changed access methods to use getArgList and setArgList
		((Term) result).setArgList((ElementList) ((Term) result).getArgList().clone());
		return result;
	}

	List<Predicate> getPredicates() {
		ArrayList<Predicate> result = new ArrayList<Predicate>();
		// fix possibility that some elements could be null
		result.add(new Predicate(getFunctor(), this.getArgList().size()));
		result.addAll(argList.getPredicates());
		return result;
	}

	/**
	 * Ensure all variables within this term that
	 * have the same public name have the same private name.
	 */
	private void consolidateVariables() {
		Map<String, String> varNames = new HashMap<String, String>();
		Iterator<Variable> iterator = this.getVariables().iterator();
		while(iterator.hasNext()) {
			Variable var = iterator.next();
			if (varNames.containsKey(var.getName())) {
				var.setInternalName(varNames.get(var.getName()));
			} else {
				varNames.put(var.getName(), var.getInternalName());
			}
		}
	}
}
