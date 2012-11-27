package nl.uu.cs.arg.shared.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a Tree of Objects of generic type T. The Tree is represented as
 * a single rootElement which points to a List<IndexedNode<T>> of children. There is
 * no restriction on the number of children that a particular node may have.
 * This Tree provides a method to serialize the Tree into a List by doing a
 * pre-order traversal. It has several methods to allow easy updation of Nodes
 * in the Tree.
 * 
 * This particular tree is indexed. It maintains a map connecting unique object
 * indexes to their {@link IndexedNode} objects. This allows for quick access to
 * nodes in the tree.
 * 
 * @author Sujit Pal
 * @author erickok
 * Taken from http://sujitpal.blogspot.com/2006/05/java-data-structure-generic-tree.html
 * Added indexing of nodes
 */
public class IndexedTree<T extends IndexedObject> {
 
	private Map<Long, IndexedNode<T>> index;
	private IndexedNode<T> rootElement;
     
    /**
     * Default ctor.
     */
    public IndexedTree() {
        super();
        index = new HashMap<Long, IndexedNode<T>>();
    }
 
    /**
     * Return the root Node of the tree.
     * @return the root element.
     */
    public IndexedNode<T> getRootElement() {
        return this.rootElement;
    }
 
    /**
     * Set the root Element for the tree.
     * @param rootElement the root element to set.
     */
    public void setRootElement(IndexedNode<T> rootElement) {
        this.rootElement = rootElement;
    }

    /**
     * Uses the index to return the {@link IndexedNode}
     * @param findIndex The unique index number of the data object contained in the {@link IndexedNode} we are looking for
     * @return The {@link IndexedNode} object that contains the object with the specified index
     */
	public IndexedNode<T> findNodeByIndex(long findIndex) {
		return index.get(findIndex);
	}
	
    /**
     * This submits the node to the index, based on the unique index number 
     * contained in the object data. Do NOT call this to add an object to the
     * tree.
     * @param node The node that was added to the tree
     */
    public void commitToIndex(IndexedNode<T> node) {
    	index.put(node.getData().getIndex(), node);
    }

    /**
     * Convenience method to submit a set of nodes that were added to the tree
     * to the index. Do NOT call this to add an object to the tree.
     * @param node The node that was added to the tree
     */
    /*public void commitToIndex(List<IndexedNode<T>> nodes) {
    	for (IndexedNode<T> node : nodes) {
    		index.put(node.getData().getIndex(), node);
    	}
    }*/
    
    /**
     * This should be called when a child is removed from the tree; this clears 
     * it form the index as well. Do NOT call this to remove an object from the 
     * tree.
     * @param node The node that was removed from the tree
     */
    public void clearFromIndex(IndexedNode<T> node) {
    	index.remove(node.getData().getIndex());
    }

    /**
     * Convenience method to clear some list of nodes that were removed from the
     * tree. Do NOT call this to remove objects from the tree itself.
     * @param node The node that was removed from the tree
     */
    public void clearFromIndex(List<IndexedNode<T>> nodes) {
    	for (IndexedNode<T> node : nodes) {
    		clearFromIndex(node);
    	}
    }
    
    /**
     * Returns the Tree<T> as a List of Node<T> objects. The elements of the
     * List are generated from a pre-order traversal of the tree.
     * @return a List<Node<T>>.
     */
   /* public List<IndexedNode<T>> toList() {
        List<IndexedNode<T>> list = new ArrayList<IndexedNode<T>>();
        walk(rootElement, list);
        return list;
    }*/
     
    /**
     * Returns a String representation of the Tree. The elements are generated
     * from a pre-order traversal of the Tree.
     * @return the String representation of the Tree.
     */
    /*public String toString() {
        return toList().toString();
    }*/
     
    /**
     * Walks the Tree in pre-order style. This is a recursive method, and is
     * called from the toList() method with the root element as the first
     * argument. It appends to the second argument, which is passed by reference     * as it recurses down the tree.
     * @param element the starting element.
     * @param list the output of the walk.
     */
    /*private void walk(IndexedNode<T> element, List<IndexedNode<T>> list) {
        list.add(element);
        for (IndexedNode<T> data : element.getChildren()) {
            walk(data, list);
        }
    }*/

    /**
     * Returns a String representation of the tree in a nice format (1 node per rule with indentation to show the hierarchical level)
     * @return The formatted string representing this tree
     */
	public String toString() {
		return prettyPrintNode(getRootElement(), "");
	}
	
	private String prettyPrintNode(IndexedNode<T> node, String identation) {
		String s =  identation + node.getData().toString().replace("\n", "") + "\n";
		for (IndexedNode<T> child : node.getChildren()) {
			s += prettyPrintNode(child, identation + "  ");
		}
		return s;
	}
	
}
