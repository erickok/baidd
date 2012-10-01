package nl.uu.cs.arg.shared.dialogue.locutions;

import org.aspic.inference.Term;

/**
 * The concede(p) locution is used to concede to some term, in effect
 * dropping any attacks. The conceded term p is a reply to some 
 * argue(A => p) move (conceding of a conclusion) or p is an element 
 * of A in some argue(A => q) move (conceding of a premise). 
 * 
 * @author erickok
 *
 */
public final class ConcedeLocution extends SurrenderingLocution {

	private static final String LOCUTION_NAME = "concede";
	
	/**
	 * Some term that was either the conclusion or a premise used in an argue(A =>p) move
	 */
	private Term concededTerm;
	
	public ConcedeLocution(Term concededTerm) {
		super(LOCUTION_NAME);
		this.concededTerm = concededTerm;
	}
	
	/**
	 * Returns the conceded term as used (as conclusion or premise) in the argue move this locution replies to
	 * @return The term that was conceded
	 */
	public Term getConcededTerm() {
		return this.concededTerm;
	}

	@Override
	public String toLogicString() {
		return getName() + "(" + getConcededTerm().inspect() + ")";
	}

}
