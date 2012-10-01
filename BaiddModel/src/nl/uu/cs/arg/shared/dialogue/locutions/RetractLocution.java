package nl.uu.cs.arg.shared.dialogue.locutions;

import org.aspic.inference.Term;

/**
 * The retract(p) locution is used to retract a previous term p
 * that was proposed as conclusion is some argue(A => p) or as a
 * premise as element of the set A in some argue(A => 1) move. In 
 * effect, the argue move that it was used in is no longer 'in'.
 * 
 * @author erickok
 *
 */
public final class RetractLocution extends SurrenderingLocution {

	private static final String LOCUTION_NAME = "retract";
	
	/**
	 * Some term that was either the conclusion or a premise used in an argue(A => p) move
	 */
	private Term retractedTerm;
	
	public RetractLocution(Term retractedTerm) {
		super(LOCUTION_NAME);
		this.retractedTerm = retractedTerm;
	}
	
	/**
	 * Returns the conceded term as used (as conclusion or premise) in the argue move this locution replies to
	 * @return The term that was conceded
	 */
	public Term getRetractedTerm() {
		return this.retractedTerm;
	}

	@Override
	public String toLogicString() {
		return getName() + "(" + getRetractedTerm().inspect() + ")";
	}
	
}
