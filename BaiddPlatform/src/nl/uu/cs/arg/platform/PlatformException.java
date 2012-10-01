package nl.uu.cs.arg.platform;

/**
 * An exception that was throw during execution of the {@link Platform}.
 * It also indicates of it was critical, which means that the platform
 * needed to halt because of it.
 * 
 * @author Eric
 */
public class PlatformException extends Exception {

	private static final long serialVersionUID = 1L;
	
	boolean isCritical;
	
	public PlatformException(String errorMessage, boolean isCritical) {
		super(errorMessage);
		this.isCritical = isCritical;
	}
	
	/**
	 * Whether this exception was critical, which means that the platform needed to halt because of it
	 * @return
	 */
	public boolean isCritical() {
		return this.isCritical;
	}

}
