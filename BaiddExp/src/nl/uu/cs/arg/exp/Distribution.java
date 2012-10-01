package nl.uu.cs.arg.exp;

/**
 * Represents a knowledge pool and agent assignment distribution
 * 
 * @author erickok
 */
public class Distribution {

	public int id;
	
	public int n_o;
	public int n_g;
	public int n_c;
	public float n_n;
	public int n_cr;
	public int n_gr;
	public int n_or;

	public int m_o;
	public int m_g;
	public int m_c;
	public int m_cr;
	public int m_gr;
	public int m_or;
	
	public Distribution(int id, int n_o, int n_g, int n_c, float n_n, int n_cr,
			int n_gr, int n_or, int m_o, int m_g, int m_c, int m_cr,
			int m_gr, int m_or) {
		this.id = id;
		this.n_o = n_o;
		this.n_g = n_g;
		this.n_c = n_c;
		this.n_n = n_n;
		this.n_cr = n_cr;
		this.n_gr = n_gr;
		this.n_or = n_or;
		this.m_o = m_o;
		this.m_g = m_g;
		this.m_c = m_c;
		this.m_cr = m_cr;
		this.m_gr = m_gr;
		this.m_or = m_or;
	}

	@Override
	public String toString() {
		return id + " [" + n_o + " " + n_g + " " + n_c + " " + n_n + " " + n_cr + " " + n_gr + " " + n_or + 
			"] [" + m_o + " " + m_g + " " + m_c + " " + m_cr + " " + m_gr + " " + m_or + "]";
	}

	public String poolBeliefsString() {
		return "[" + n_o + " " + n_g + " " + n_c + "]";
	}

	public String poolRulesString() {
		return "[" + n_cr + " " + n_gr + " " + n_or + "]";
	}

	public String agentBeliefsString() {
		return "[" + m_o + " " + m_g + " " + m_c + "]";
	}

	public String agentRulesString() {
		return "[" + m_cr + " " + m_gr + " " + m_or + "]";
	}

	public String factNegationString() {
		return "[" + n_n + "]";
	}

}
