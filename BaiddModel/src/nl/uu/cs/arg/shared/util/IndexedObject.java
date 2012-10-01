package nl.uu.cs.arg.shared.util;

/**
 * An IndexedObject is simply any object that is able to return
 * a unique index (for the group off all instantiations of that 
 * type).
 * 
 * @author erickok
 *
 */
public interface IndexedObject {

	/**
	 * The unique index for this object (for all instantiations of this type)
	 * @return The long index
	 */
	public long getIndex();
	
}
