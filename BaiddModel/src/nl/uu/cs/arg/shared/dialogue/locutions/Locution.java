package nl.uu.cs.arg.shared.dialogue.locutions;

/**
 * An abstract locution, only providing a name. Subclasses should 
 * provide content as well which is specific to that locution,
 * such as argument data in an argue(A => p) locution.
 * 
 * @author erickok
 *
 */
public abstract class Locution {

	/**
	 * The public, human readable name.
	 */
	private String name;
	
	public Locution(String name) {
		this.name = name;
	}
	
	/**
	 * Returns the public, human readable name
	 * @return The name of this locution
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Every locution should be able to return a string that represents the 
	 * logical notation of this locution and all its contents (e.g. 'why(p)')
	 * @return A formatted and human-readable string
	 */
	public abstract String toLogicString();
	
}
