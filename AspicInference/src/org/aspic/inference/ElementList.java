package org.aspic.inference;

import java.util.*;

import org.aspic.inference.writers.*;

/**
 * An encapsulated <code>List</code> of <code>Element</code>s that is used as the antecedent in
 * a <code>Rule</code>, the <code>argList</code> of a <code>Term</code>, and could possibly be used to capture 
 * lists if list processing operators were built into the language.
 * 
 * @author mjs (matthew.south @ cancer.org.uk)
 */
public class ElementList extends Element implements Iterable<Element> {
	private List<Element> list = new ArrayList<Element>();

    //Henrik
    public List<Element> getList() {
        return list;
    }

    public void setList(List<Element> list) {
        this.list = list;
    }   
    
    
	/**
	 * Default Constructor.
	 */
	public ElementList() {
		
	}
	
	/**
	 * Typical constructor.  Allows variable number of elements to be added at construction.
	 * @param elements var-args list of Elements
	 */
	public ElementList(Element... elements) { 
		for (Element element: elements) {	
			list.add(element);
		}
		consolidateVariables();
	}
    /*
    public String toStringXml(){
        StringBuffer result = new StringBuffer("<elementList>");
        int index = 0;
        for (Iterator<Element> it = list.iterator(); it.hasNext(); index++){
            Element element = it.next();
            result.append("<element_" + index + ">" + element.toStringXml() + "</element_" + index + ">");
        }
        result.append("</elementList>");
        return result.toString();
    }
    */

	/** 
	 * Add new Element to end of ElementList.
	 * @param c element to add to this ElementList.
	 * @return true if list was changed.
	 **/
	public boolean add(Element c) { 
		list.add(c);
		consolidateVariables();
		return true; 
	}

	/** 
	 * Add new Element to ElementList at specified index (pushes higher Elements up).
	 * @param c element to add to this ElementList.
	 * @param index position to add element.
	 * */
	public void add(int index, Element c) { 
		list.add(index, c);
		consolidateVariables();
	}

	/** 
	 * set new Element to ElementList at specified index
	 * @param c element to add to this ElementList
	 * @param index position to add element
	 * @return reference to enhanced ElementList  
	 * */
	public Element set(int index, Element c) { 
		Element result = list.set(index, c); 
		consolidateVariables();
		return result; 
	}

	/** 
	 * Retrieve Element by index. 
	 * @param index index of SimpleClause to retrive 0 <= index < size 
	 * @return requested SimpleClause
	 * @exception IndexOutOfBoundsException thrown if index < 0 or index >= size 
	 */
	public Element get(int index) {
		if (index<list.size() && index>=0)
			return list.get(index);
		else
			throw new IndexOutOfBoundsException();
	}
	
	/** 
	 * Retrieve size of list 
	 * @return size of list
	 * */
	public int size() { return list.size(); }
	
	/** 
	 * remove Element at position index 
	 * @param index 0..(size()-1)
	 * @return removed Element
	 * */
	public Element remove(int index) {
		return list.remove(index);
	}
	
	/** 
	 * Provide list iterator 
	 * @return Element iterator over contents of list
	 * */
	public Iterator<Element> iterator() {
		return list.iterator();
	}
	
	/**
	 * Returns comma delimited string of Elements with no enclosing brackets
	 * @see org.aspic.inference.Element#inspect()
	 */
	public String inspect() {
		String expression = "";
		if (this.list.size()>0) {
			Iterator<Element> itr = list.iterator();
			while (itr.hasNext()) {
				expression += itr.next().inspect() + (itr.hasNext() ? ", " : "");
			}
		}
		return expression;
	}

	/**
	 * Clone ElementList and apply substitution to all elements within the clone.
	 * @param subs substitution to apply
	 * @return new ElementList with the passed substitution applied.
	 */
	public ElementList apply(Substitution subs) {
		ElementList newList = new ElementList();
		newList.setKnowledgeBase(this.getKnowledgeBase());
		Iterator<Element> itr = this.iterator();
		while (itr.hasNext()) {
			newList.add( itr.next().apply(subs) );
		}
		return newList;
	}
	
	public boolean isUnifiable(Element toUnify) {
		Substitution subs = new Substitution();
		if (((toUnify instanceof ElementList) && 
				(((ElementList) toUnify).size()==this.size()))) {
			for (int i=0; i<this.size(); i++) {
				if (!this.get(i).isUnifiable(((ElementList) toUnify).get(i))) return false;
				else subs = this.get(i).unify(((ElementList) toUnify).get(i), subs);
			}
			// getting here means that the ElementList had the right shape.
			// the final hurdle is to check for unification problems in the variables.
			return subs.isConsistant();
		}
		return false;
	}
	
	public Substitution unify(Element toUnify, Substitution subs) {;
		// Only makes sense to unify with an ElementList
		if (toUnify instanceof ElementList) {
			if (((ElementList) toUnify).size()!=this.size()) return subs;
			for (int i=0; i<this.size(); i++) {
				subs = this.get(i).unify(((ElementList) toUnify).get(i), subs);
			}
		}		
		return subs;
	}

	boolean isEqualModuloVariables(Element testList) {
		if (testList instanceof ElementList) {
			if (((ElementList) testList).size()!=this.size()){
                return false;
            }
			for (int i=0; i<this.size(); i++) {
				if (this.get(i)==null || ((ElementList) testList).get(i)==null || this.get(i).isEqualModuloVariables(((ElementList) testList).get(i)) == false) { 
					return false; 
				}
			}
			return true;			
		} else {
			return false;
		}
	}
	
	/**
	 * Checks to see if this list contains a particular Element.
	 * @param testElement the Element that your looking for
	 * @return true if the test Element is in the list.
	 */
	public boolean containsModuloVariables(Element testElement) {
		for (int i=0; i<this.size(); i++) {
			if (this.get(i).isEqualModuloVariables(testElement)) { 
				return true; 
			}
		}		
		return false;
	}
	
	/**
	 * @return true iff all members are grounded.
	 */
	boolean isGrounded() {
		Iterator<Element> itr = this.iterator();
		while (itr.hasNext()) {
			if (itr.next().isGrounded()==false) { return false; }
		}
		return true;
	}

	public void write(KnowledgeWriter writer) {
		writer.write(this);
	}
	
	/*
	public String argumentTag(Double needed) {
		// TODO: Needed? - suggested implementation is join all Clauses with a ", "
		String result = "(";
		Iterator<Element> itr = list.iterator();
		while (itr.hasNext()) {
			Element next = itr.next();
			result += next.argumentTag(needed);
			if (itr.hasNext()) result += ", ";
		}
		return result + ")_" + needed.toString();
	}
	*/
	
	public void setKnowledgeBase(KnowledgeBase kb) {
		super.setKnowledgeBase(kb);
		Iterator<Element> itr = list.iterator();
		while (itr.hasNext()) {
			Element element = itr.next();
			if (element!=null) element.setKnowledgeBase(kb);
		}
	}
	
	List<Variable> getVariables() {
		List<Variable> varList = new ArrayList<Variable>();
		Iterator<Element> iterator = list.iterator();
		while (iterator.hasNext()) {
			Element element = iterator.next();
			if (element!=null) varList.addAll(element.getVariables());
		}
		return varList;
	}

	public Object clone() throws CloneNotSupportedException {
		Object result = super.clone();
        List<Element> cloneList = new ArrayList();
        for ( Element element : ((ElementList)result).getList()){
            cloneList.add((Element)element.clone());
        }
		((ElementList) result).setList((List<Element>) cloneList);
        //((ElementList) result).list = (List<Element>) ((ArrayList) ((ElementList) result).list).clone();
		for (int i=0; i<((ElementList) result).size(); i++) {
			if (((ElementList) result).list.get(i)!=null) {
				((ElementList) result).list.set(i, (Element) ((ElementList) result).list.get(i).clone());
			}
		}
		return result;
	}
/*
	public boolean equals(Object o) {
		return (o instanceof ElementList) && ((ElementList) o).isEqualModuloVariables(this);
	}
*/
	public boolean equals(Object o) {
		return (o instanceof ElementList) && ((ElementList) o).isEqualModuloVariables(this);
	}

	public int hashCode() {
		int result = 17;
		Iterator<Element> itr = this.list.iterator();
		while (itr.hasNext()) {
			Element element = itr.next();
			result = 37*result + element.hashCode();
		}
		return result;
	}

	/**
	 *  Return a copy of this ElementList containing all but the last clause
	 */
	ElementList longHead() {
		ElementList tmpList = new ElementList();
		tmpList.setKnowledgeBase(this.getKnowledgeBase());
		Iterator<Element> itr = list.iterator();
		while (itr.hasNext()) {
			Element tmpClause = itr.next();
			if (itr.hasNext()) { 
				tmpList.add(tmpClause); 
			}
		}
		return tmpList;
	}
	
	/** 
	 * Return the last clause in this list
	 */
	Element shortTail() {
		return this.get(this.size()-1);
	}
	
	/**
	 * Use this method to get all the different arguments that can be
	 * discovered for this ElementList.
	 * @param needed ignores all arguments with support less than this value
	 * @param party used for logging
	 * @param level used for prettifying logging (needed?)
	 * @param d_top used
	 * @return Iterator for all ArgumentLists that can be generated for this ElementList
	 */
	Iterator<RuleArgumentList> argumentIterator(Double needed, Party party, int level, int d_top, RuleArgumentValuator valuator, boolean restrictedRebutting) {
		if (this.size()==0) {
			return new EmptyArgumentListIterator();
		} else {
			return new ArgumentListIterator(needed, party, level, d_top, valuator, restrictedRebutting);
		}
	}
	
	/**
	 * Ensure all variables within this term that
	 * have the same public name have the same private name.
	 */
	void consolidateVariables() {
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

	/**
	 * <p>Tricky Argument iterator. Return all ArgumentLists that can be generated
	 * based on this ElementList.  It's tricky because of it's combinatorial 
	 * nature.  If your elementlist consists of two terms, t1, t2 and you can 
	 * develop two arguments for each term, i.e. t1a1, t1a2, t2a1 and t2a2 then
	 * this argumentListIterator must return four possible argument lists:
	 * [t1a1, t2a1], [t1a1, t2a2], [t1a2, t2a1] and [t1a2, t2a2].</p> 
	 * 
	 * <p>To implement this we define the argumentListIterator recursively:
	 * A request for the argument list iterator over the list, 
	 * <code>[e_1, e_2, ... , e_n]</code>
	 * in turn requests for the argument list iterator over the sub list,
	 * <code>[e_1, e_2, ... , e_(n-1)]</code> (known as the longhead list)
	 * and then iterates through the arguments generated for e_n (the shorttail) and
	 * adds them on to the argument lists returned from the recursively
	 * defined sub list argument list iterator.</p>
	 * @author mjs (matthew.south @ cancer.org.uk)
	 *
	 */
	private class ArgumentListIterator implements Iterator<RuleArgumentList> {
		private Double needed;
		private Party party;
		private int level;
		private int d_top;
		private RuleArgumentValuator valuator;
		private boolean restrictedRebutting;
		
		private Iterator<RuleArgumentList> argumentListIterator;
		private Iterator<RuleArgument> argumentIterator;
		private RuleArgumentList currentSubList;
		
		private RuleArgumentList nextArgumentList = null;
		private boolean queuedArgumentList = false;

		public ArgumentListIterator(Double needed, Party party, int level, int d_top, RuleArgumentValuator valuator, boolean restrictedRebutting) {
			this.needed = needed;
			this.party = party;
			this.level = level;
			this.d_top = d_top;
			this.valuator = valuator;
			this.restrictedRebutting = restrictedRebutting;
			
			argumentListIterator = ElementList.this.longHead().argumentIterator(needed, party, level, d_top, valuator, restrictedRebutting);
			if (argumentListIterator.hasNext()) {
				currentSubList = argumentListIterator.next();
				argumentIterator = ((Constant) ElementList.this.shortTail()).apply(currentSubList.getSubstitution()).argumentIterator(needed, party, level, d_top, valuator, restrictedRebutting);
			}
			queuedArgumentList = this.hasNext();
		}
		
		public boolean hasNext() {
			if (queuedArgumentList==true) {
				return true;
			} else {
				if (!(argumentIterator==null) && argumentIterator.hasNext()) {
					nextArgumentList = currentSubList.cloneAndExtend(argumentIterator.next());
					queuedArgumentList=true;
					return true;
				} else {
					if (argumentListIterator.hasNext()) {
						currentSubList = argumentListIterator.next();
						argumentIterator = ((Constant) ElementList.this.shortTail()).apply(currentSubList.getSubstitution()).argumentIterator(needed, party, level, d_top, valuator, restrictedRebutting);
						return this.hasNext();
					} else {
						return false;
					}
				}				
			}
		}
		
		public RuleArgumentList next() {
			if (queuedArgumentList) {
				queuedArgumentList = false;
				return nextArgumentList;
			} else {
				throw new NoSuchElementException();
			}
		}
		
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	/**
	 * Returns an Iterator over a list with one empty ArgumentList in it. 
	 */
	private class EmptyArgumentListIterator implements Iterator<RuleArgumentList> {
		private boolean flag;
		public EmptyArgumentListIterator() {
			flag = true;
		}
		public boolean hasNext() {
			return flag;
		}
		public RuleArgumentList next() {
			flag=false;
			return new RuleArgumentList();
		}
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	/**
	 * Make a shallow copy of this ElementList.  i.e. return a new
	 * list that points to the same Elements.
	 * @return shallow copy of this ElementList
	 */
	Object copy() {
		ElementList clone = new ElementList();
		Iterator<Element> itr = list.iterator();
		while (itr.hasNext()) {
			clone.add(itr.next());
		}
		clone.setKnowledgeBase(this.getKnowledgeBase());
		return clone;
	}

	@Override
	List<Predicate> getPredicates() {
		ArrayList<Predicate> result = new ArrayList<Predicate>();
		Iterator<Element> iterator = list.iterator();
		while (iterator.hasNext()) {
			Element element = iterator.next();
			if (element!=null) result.addAll(element.getPredicates());
		}
		return result;
	}
}

/*
I found this iterator difficult to design, so I set up this simplified
exercise to help me:

The task in hand has a combinatorial nature. The input is a list of objects [a, b, c].  
Each member can be replaced by several alternatives, and the task is to return all possible 
combinations (lists) of the alternatives.  Thus if each member has three alternatives and 
the alternatives for a were written as a1, a2 and a3, then given the input [a, b, c], we 
should expect the output:
[a1, b1, c1]
[a1, b1, c2]
[a1, b1, c3]
[a1, b2, c1]
...
[a3, b3, c3]

In my example I define an IntegerFoo class that encapsulates an Integer i and has a foo() method that returns an iterator of alternative Integer objects (with values i+1, i+2, i+3).  The class that does the work is then an ArrayList of IntegerFoo objects (ArrayListFoo) and the expected output is another Iterator (returned by another .foo() method) that returns ArrayLists of Integers.

The generalised algorithm for the iterator ArrayListFoo.foo() recursively calls .foo() on a smaller ArrayListFoo (one without the final element) and adds the alternatives of the final element to each element of the returned iterator.   

so the implemented algorithm could be sketched...
input = [i_1, i_2, ... , i_n].foo
	itrsublist = [i_1, i_2, ... , i_(n-1)].foo
	itrtail = i_n.foo()
	for each member of itrsublist
		for each member of itrtail
			add itrtail.next to the end of itrsublist.next
*/

/*
class FancyArray {
	public static void main(String[] args) {
		ArrayListFoo arrayListFoo = new ArrayListFoo();
		arrayListFoo.add(new IntegerFoo(0));
		arrayListFoo.add(new IntegerFoo(5));
		arrayListFoo.add(new IntegerFoo(10));

		Iterator<ArrayList<Integer>> itr2 = arrayListFoo.foo();
		while (itr2.hasNext()) {
			System.out.println(itr2.next());
		}
	}
}

class IntegerFoo {
	private Integer value;
	IntegerFoo(int value) {
		this.value = new Integer(value);
	}
	Iterator<Integer> foo() {
		List<Integer> list = new ArrayList<Integer>();
		list.add(new Integer(value+1));
		list.add(new Integer(value+2));
		list.add(new Integer(value+3));
		return list.iterator();
	}
	public String toString() {
		return value.toString();
	}
}

class ArrayListFoo extends ArrayList<IntegerFoo> {

	Iterator<ArrayList<Integer>> foo() {
		if (this.size()==0) {
			return new EmptyArrayListIntegerIterator();
		} else {
			return new ArrayListIntegerIterator();
		}
	}
	
	// Return a List of all but the last entry
	ArrayListFoo longHead() {
		ArrayListFoo temp = (ArrayListFoo) this.clone();
		temp.remove(temp.size()-1);
		return temp;
	}
	
	// Return the last entry in the list
	IntegerFoo shortTail() {
		return this.get(this.size()-1);
	}
	
	private class ArrayListIntegerIterator implements Iterator<ArrayList<Integer>> {
		
		// Iterator for alternatives for end element of this list
		private Iterator<Integer> shortTailIterator; 
		// Iterator for alternative combinations of the longHead subList
		private Iterator<ArrayList<Integer>> longHeadIterator; 
		// "cursor" - current position in longHead subList
		private ArrayList<Integer> currentSubList; 
		
		public ArrayListIntegerIterator() {
			// Caution: Recursion in the next line
			longHeadIterator = ArrayListFoo.this.longHead().foo();
			currentSubList = longHeadIterator.next();
			shortTailIterator = ArrayListFoo.this.shortTail().foo();
		}
		
		public boolean hasNext() {
			return (longHeadIterator.hasNext() || shortTailIterator.hasNext());
		}
		
		public ArrayList<Integer> next() {
			if (shortTailIterator.hasNext()) {
				ArrayList<Integer> clonedArray = (ArrayList<Integer>) currentSubList.clone();
				clonedArray.add(shortTailIterator.next());
				return clonedArray;
			} else {
				if (longHeadIterator.hasNext()) {
					currentSubList = longHeadIterator.next();
					shortTailIterator = ArrayListFoo.this.shortTail().foo();
					return this.next();
				} else {
					// TODO: raise more specific error!
					throw new RuntimeException();
				}			
			}							
		}
		
		public void remove() {
			throw new UnsupportedOperationException();
		}
	} 
	
 
	//An empty ArrayList iterator that returns one empty ArrayList.
	private class EmptyArrayListIntegerIterator implements Iterator<ArrayList<Integer>> {
		boolean flag;
		// Constructor
		public EmptyArrayListIntegerIterator() {
			flag=true;
		}
		
		public boolean hasNext() {
			return flag;
		}
		
		public ArrayList<Integer> next() {
			flag=false;
			return new ArrayList<Integer>();
		}
		
		public void remove() {
			throw new UnsupportedOperationException();
		}
	} 
}
*/
