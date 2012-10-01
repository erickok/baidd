package nl.uu.cs.arg.shared.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a node of the Tree<T> class. The IndexedNode<T> is also a container, and
 * can be thought of as instrumentation to determine the location of the type T
 * in the Tree<T>.
 * 
 * This particular type of node is indexed. It will call the parent tree on 
 * instantiating and when removing children so it can keep track of the indexes of the 
 * nodes that are contained in the tree - providing quick access. 
 * 
 * @author Sujit Pal
 * @author erickok
 * Taken from http://sujitpal.blogspot.com/2006/05/java-data-structure-generic-tree.html
 * Added indexing of nodes
 */
public class IndexedNode<T extends IndexedObject> {
 
	private IndexedTree<T> parentTree;
	protected T data;
    protected List<IndexedNode<T>> children;
 
    /**
     * Convenience ctor to create a Node<T> with an instance of T.
     * @param data an instance of T.
     */
    public IndexedNode(IndexedTree<T> parentTree, T data) {
        this.parentTree = parentTree;
        setData(data);
        parentTree.commitToIndex(this);
    }

    /**
     * Return the children of IndexedNode<T>. The IndexedTree<T> is represented by a single
     * root IndexedNode<T> whose children are represented by a List<IndexedNode<T>>. Each of
     * these IndexedNode<T> elements in the List can have children. The getChildren()
     * method will return the children of a IndexedNode<T>.
     * @return the children of IndexedNode<T>
     */
    public List<IndexedNode<T>> getChildren() {
        if (this.children == null) {
            return new ArrayList<IndexedNode<T>>();
        }
        return this.children;
    }
 
    /**
     * Sets the children of a IndexedNode<T> object. See docs for getChildren() for
     * more information.
     * @param children the List<IndexedNode<T>> to set.
     */
    public void setChildren(List<IndexedNode<T>> children) {
    	// The old nodes need to be removed form the index first, because these nodes will no longer exist
    	parentTree.clearFromIndex(this.children);
    	// Set the new children, which are already in the index since they were instantiated
        this.children = children;
    }
 
    /**
     * Returns the number of immediate children of this IndexedNode<T>.
     * @return the number of immediate children.
     */
    public int getNumberOfChildren() {
        if (children == null) {
            return 0;
        }
        return children.size();
    }
     
    /**
     * Adds a child to the list of children for this IndexedNode<T>. The addition of
     * the first child will create a new List<IndexedNode<T>>.
     * @param child a IndexedNode<T> object to set.
     */
    public void addChild(IndexedNode<T> child) {
        if (children == null) {
            children = new ArrayList<IndexedNode<T>>();
        }
        children.add(child);
    }
     
    /**
     * Inserts a IndexedNode<T> at the specified position in the child list. Will
     * throw an ArrayIndexOutOfBoundsException if the index does not exist.
     * @param index the position to insert at.
     * @param child the IndexedNode<T> object to insert.
     * @throws IndexOutOfBoundsException if thrown.
     */
    public void insertChildAt(int index, IndexedNode<T> child) throws IndexOutOfBoundsException {
        if (index == getNumberOfChildren()) {
            // this is really an append
            addChild(child);
            return;
        } else {
            children.get(index); //just to throw the exception, and stop here
            children.add(index, child);
        }
    }
     
    /**
     * Remove the IndexedNode<T> element at index index of the List<IndexedNode<T>>.
     * @param index the index of the element to delete.
     * @throws IndexOutOfBoundsException if thrown.
     */
    public void removeChildAt(int index) throws IndexOutOfBoundsException {
        parentTree.clearFromIndex(children.get(index));
        children.remove(index);
    }
 
    public T getData() {
        return this.data;
    }
 
    public void setData(T data) {
        this.data = data;
    }
     
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{").append(getData().toString()).append(",[");
        int i = 0;
        for (IndexedNode<T> e : getChildren()) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(e.getData().toString());
            i++;
        }
        sb.append("]").append("}");
        return sb.toString();
    }
}
