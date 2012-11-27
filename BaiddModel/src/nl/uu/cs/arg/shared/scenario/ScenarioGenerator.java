package nl.uu.cs.arg.shared.scenario;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aspic.inference.Constant;
import org.aspic.inference.Element;
import org.aspic.inference.ElementList;
import org.aspic.inference.Rule;
import org.aspic.inference.Term;

public class ScenarioGenerator {

	public class InvalidConfigurationException extends Exception {
		private static final long serialVersionUID = -5344517207171622228L;

		public InvalidConfigurationException(String message) {
			super(message + " (for " + ScenarioGenerator.this.toString() + ")");
		}
	}

	public class CausesLoopException extends InvalidConfigurationException {
		private static final long serialVersionUID = -2033314128066641997L;

		public CausesLoopException() {
			super("Knowledge assigned to the agents causes rule loops!");
		}
	}

	public enum ConflictMethod {
		Direct, Chained
	}

	public enum AssignmentMethod {
		Randomly, Evenly
	}

	public class Pool {
		public Pool(List<Term> O, List<Constant> G, List<Constant> B) {
			this.O = O;
			this.G = G;
			this.B = B;
		}

		public Pool(List<Term> O, List<Constant> G) {
			this.O = O;
			this.G = G;
		}

		public Pool() {
		}

		public List<Term> O;
		public List<Constant> G;
		public List<Constant> B;
	}

	private final String g_d;
	private final int n_A;
	private final int n_R;
	private final int n_B_s;
	private final int n_O_s;
	private final int n_G_s;
	private final int n_O_r;
	private final int n_G_r;
	private final int l;
	private final int n_G_nro;
	private final int n_B_ra;
	private final int n_B_nra;
	private final AssignmentMethod beliefAssignment;
	private final AssignmentMethod roleAssignment;
	private final ConflictMethod conflictMethod;

	private List<Rule> kb = new ArrayList<Rule>();
	private int ruleCounter = 0;

	public ScenarioGenerator(String g_d, int n_A, int n_R, int n_B_s,
			int n_O_s, int n_G_s, int n_O_r, int n_G_r, int l, int n_G_nro,
			int n_B_ra, int n_B_nra, AssignmentMethod assignment,
			AssignmentMethod roleAssignment, ConflictMethod conflictMethod) {
		this.g_d = g_d;
		this.n_A = n_A;
		this.n_R = n_R;
		this.n_B_s = n_B_s;
		this.n_O_s = n_O_s;
		this.n_G_s = n_G_s;
		this.n_O_r = n_O_r;
		this.n_G_r = n_G_r;
		this.l = l;
		this.n_G_nro = n_G_nro;
		this.n_B_ra = n_B_ra;
		this.n_B_nra = n_B_nra;
		this.beliefAssignment = assignment;
		this.roleAssignment = roleAssignment;
		this.conflictMethod = conflictMethod;
	}

	public List<Pool> generate() throws InvalidConfigurationException {

		// Generate context
		String topic = "do";
		Constant gd = new Constant(g_d);
		List<Constant> B_s = new ArrayList<Constant>();
		List<Term> O_s = new ArrayList<Term>();
		List<Constant> G_s = new ArrayList<Constant>();
		for (int i = 0; i < n_B_s; i++) {
			B_s.add(new Constant("p_" + i));
		}
		for (int i = 0; i < n_O_s; i++) {
			O_s.add(new Term(topic, new Constant("o_" + i)));
		}
		for (int i = 1; i < n_G_s; i++) {
			G_s.add(new Constant("g_" + i));
		}

		// Generate roles
		List<Pool> roles = new ArrayList<Pool>();
		for (int r = 0; r < n_R; r++) {
			roles.add(new Pool(assign(O_s, n_O_r, "n_O_r"), assign(G_s, n_G_r, "n_G_r")));
		}

		// Generate knowledge pool
		Set<Term> O_K = new HashSet<Term>();
		Set<Constant> G_K = new HashSet<Constant>();
		G_K.add(gd);
		for (Pool role : roles) {
			// Note that since this uses sets, it will get rid of duplicates
			O_K.addAll(role.O);
			G_K.addAll(role.G);
		}
		Pool knowledge = new Pool(new ArrayList<Term>(O_K), new ArrayList<Constant>(G_K));

		// Generate role-option beliefs using chaining
		Set<Constant> B_K = new HashSet<Constant>();
		for (Pool role : roles) {
			role.B = new ArrayList<Constant>();
		}
		for (Term o : O_K) {

			List<Set<Rule>> chains = new ArrayList<Set<Rule>>();

			// Generate rule chains for the roles that are assigned this option
			for (Pool role : roles) {
				if (role.O.contains(o)) {

					// Generate rule chain
					// Randomize the beliefs seedset and the role's goals (so we can
					// pick a random one to generate this chain to)
					Collections.shuffle(B_s);
					Collections.shuffle(role.G);
					Set<Rule> C_go = new HashSet<Rule>();
					Constant last = o;
					for (int i = 0; i < l; i++) {
						// Form a single rule
						if (i == l - 1) {
							// Last rule of the chain: end with the goal as conclusion
							C_go.add(newRule(role.G.get(0), last));
						} else {
							// Create a chain from the last conclusion (or the option) to some random literal
							C_go.add(newRule(B_s.get(i), last));
							last = B_s.get(i);
						}
					}

					// Generate rule chain for the mutual goal
					Collections.shuffle(B_s);
					Set<Rule> C_gd = new HashSet<Rule>();
					Constant last2 = o;
					for (int i = 0; i < l; i++) {
						// Form a single rule
						if (i == l - 1) {
							// Last rule of the chain: end with the mutual goal as conclusion
							C_gd.add(newRule(gd, last2));
						} else {
							// Create a chain from the last conclusion (or the option) to some random literal
							C_gd.add(newRule(B_s.get(i), last2));
							last2 = B_s.get(i);
						}
					}

					// Remember this chain to generate negated beliefs for later on
					chains.add(C_go);
					chains.add(C_gd);

					// And add to the role beliefs, plus the total set of beliefs
					role.B.addAll(C_go);
					role.B.addAll(C_gd);
					B_K.addAll(C_go);
					B_K.addAll(C_gd);

				}
			}

			// Generate negated beliefs for the roles that were not assigned this option
			if (chains.size() > 0) {
				for (Pool role : roles) {
					if (!role.O.contains(o)) {

						// Randomize the list of chains so we pick one at random to generate negated beliefs for
						Collections.shuffle(chains);
						// Define all negations for every rule in the chain
						Set<Rule> B_nro = new HashSet<Rule>();
						for (Rule rule : chains.get(0)) {
							Constant antecedent = (Constant) rule.getAntecedent().get(0);
							Constant consequent = rule.getConsequent();
							if (!consequent.isEqualModuloVariables(gd)) {
								B_nro.addAll(addConflicts(rule.getConsequent().negation(), B_s)); // Rebuttal
							}
							if (!antecedent.isEqualModuloVariables(o)) {
								B_nro.addAll(addConflicts(antecedent.negation(), B_s)); // Underminer
							}
							B_nro.addAll(addConflicts(rule.getName().negation(), B_s)); // Undercutter
						}

						// Add a subset of all the negated beliefs as this role-option beliefs
						//List<Rule> bnro = assign(new ArrayList<Rule>(B_nro), n_B_nro, "n_B_nro");
						role.B.addAll(B_nro);
						B_K.addAll(B_nro);

					}
				}
			}

		}

		// Complete knowledge pool beliefs
		knowledge.B = new ArrayList<Constant>(B_K);

		// Allocate a role and appropriate options and goals to the agents
		List<Pool> agents = new ArrayList<Pool>();
		int r = 0; // Used to evenly assign roles
		for (int a = 0; a < n_A; a++) {

			// Assign a role
			Pool role;
			if (roleAssignment == AssignmentMethod.Randomly) {
				// Get a random role
				Collections.shuffle(roles);
				role = roles.get(0);
			} else {
				// Get the next not-yet-assigned role, or start over again if we
				// used up all roles
				if (r >= roles.size()) {
					r = 0;
				}
				role = roles.get(r);
				r++;
			}

			// Add (role-originating) options
			List<Term> O_a = new ArrayList<Term>(role.O);

			// Add role and non-role originating goals
			List<Constant> G_a = new ArrayList<Constant>(role.G);
			G_a.add(gd); // And always the mutual goal
			List<Constant> G_nr = new ArrayList<Constant>(knowledge.G);
			G_nr.removeAll(role.G);
			G_a.addAll(assign(G_nr, n_G_nro, "n_G_nro")); // And non-role goals

			// Define all role-originating beliefs
			List<Constant> B_ra = new ArrayList<Constant>(role.B);

			// Define all non-role originating beliefs
			List<Constant> B_nra = new ArrayList<Constant>();
			List<Set<Rule>> chains = new ArrayList<Set<Rule>>();
			for (Term o : O_K) {
				if (role.O.contains(o)) {

					// Generate rule chain
					// Randomize the beliefs seedset and the agent's goals (so we can pick a random one to generate this
					// chain to)
					Collections.shuffle(B_s);
					Collections.shuffle(G_a);
					Set<Rule> C_go = new HashSet<Rule>();
					Constant last = o;
					for (int i = 0; i < l; i++) {
						// Form a single rule
						if (i == l - 1) {
							// Last rule of the chain: end with the goal as conclusion
							C_go.add(newRule(role.G.get(0), last));
						} else {
							// Create a chain from the last conclusion (or the option) to some random literal
							C_go.add(newRule(B_s.get(i), last));
							last = B_s.get(i);
						}
					}
					chains.add(C_go);
					B_nra.addAll(C_go);

				} else {
					
					Collections.shuffle(B_s);
					// From all beliefs in the context, either assign the negation of one or generate a conflict chain
					List<Constant> S = new ArrayList<Constant>(B_s);
					Constant b = S.remove(0);
					B_nra.addAll(addConflicts(b.negation(), S));
					//B_K.addAll();

				}
			}

			// Assign the agent's role and non-role originating beliefs
			Set<Constant> B_a = new HashSet<Constant>(assign(B_ra, n_B_ra, "n_B_ra"));
			B_a.addAll(assign(B_nra, n_B_nra, "n_B_nra"));

			// Check for loops in the agent knowledge
			List<Rule> checked = new ArrayList<Rule>();
			for (Constant b : B_a) {
				if (!(b instanceof Rule) || (b instanceof Rule && !causesLoop(checked, (Rule) b))) {
					checked.add((Rule) b);
				} else {
					throw new CausesLoopException();
				}
			}

			agents.add(new Pool(O_a, G_a, new ArrayList<Constant>(B_a)));
		}

		return agents;

	}

	private Collection<Rule> addConflicts(Constant negation, List<Constant> B_s) throws InvalidConfigurationException {
		if (conflictMethod == ConflictMethod.Direct) {
			// Directly return the negation as fact
			return Arrays.asList(newRule(negation));
		} else {
			if (l > B_s.size()) {
				// We need l - 1 atoms to generate new chain rules and 1 additional atom as chain premise/fact 
				throw new InvalidConfigurationException("Conflict chain length was set to " + l
						+ " but the list B_s from which to build it is only " + B_s.size() + " long.");
			}
			// Generate a chain with the negation as conclusion
			Collections.shuffle(B_s);
			Set<Rule> nC = new HashSet<Rule>();
			Constant last = B_s.get(0);
			for (int i = 0; i < l; i++) {
				// Form a single rule
				if (i == l - 1) {
					// Last rule of the chain: end with the mutual goal as conclusion
					nC.add(newRule(negation, last));
				} else {
					// Create a chain from the last conclusion (or the option) to some random literal
					nC.add(newRule(B_s.get(i + 1), last));
					last = B_s.get(i + 1);
				}
			}
			// Also add the fact to use as rule chain premise
			nC.add(newRule(B_s.get(0)));
			return nC;
		}
	}

	private Rule newRule(Constant constant, Element... list) {
		return newRule(1.0D, constant, list);
	}

	private Rule newRule(double d, Constant constant, Element... list) {
		// Do we already have a rule for this conclusion and antecedents?
		for (Rule rule : kb) {
			if (rule.getConsequent().isEqualModuloVariables(constant) && rule.getAntecedent().size() == list.length) {
				if (rule.getAntecedent().isUnifiable(new ElementList(list))) {
					// A ruel is found that is unifyable and therefor similar to the new one: return this rule
					return rule;
				}
			}
		}
		// Construct a new rule, generating an explicit name
		Rule r = new Rule(constant, new ElementList(list), d, new Constant("r" + ++ruleCounter));
		// Add to the existing knowledge and return
		kb.add(r);
		return r;
	}

	/**
	 * Look whether a new rule would cause a loop when applying rules (which is a way of circular reasoning not
	 * supported by the AspicInference project)
	 * @param currentRules The current rule pool
	 * @param newRule The new rule we are looking to add
	 * @return True if the new rule would cause a loop, false otherwise
	 */
	private boolean causesLoop(List<Rule> currentRules, Rule newRule) {
		List<Rule> applied = new ArrayList<Rule>();
		List<Rule> newRules = new ArrayList<Rule>(currentRules);
		newRules.add(newRule);
		applied.add(newRule);
		return causesLoop(newRules, applied, newRule);
	}

	// Used internally to recursively look for loops in the rules
	private boolean causesLoop(List<Rule> allRules, List<Rule> applied, Rule testRule) {

		// For each antecedent
		for (Element a : testRule.getAntecedent()) {
			// Check if there is a rule that can be applied
			for (Rule r : allRules) {
				if (r.getConsequent().isUnifiable(a)) {

					if (applied.contains(r)) {
						// If we already applied this rule earlier, we have a loop!
						return true;
					} else {
						// Not applied yet: add it to applied and look it it can cause a loop itself
						applied.add(r);
						if (causesLoop(allRules, applied, r)) {
							return true;
							// If not, continue looking
						}
					}

				}
			}
		}
		// None of the rule that we needed to apply caused a loop
		return false;
	}

	/**
	 * Provides a new list of n elements from the input list, which may be the first or a random selection based on the
	 * local {@link AssignmentMethod} setting
	 * @param <T> The type of elements in the list
	 * @param list The input list, which should be at least as long as n
	 * @param n The number of items in the output list
	 * @param errorVar The name of the variable used as input for n; used to generate error messages
	 * @return The new list of n number of elements, possibly picked at random
	 * @throws InvalidConfigurationException Thrown when a configuration is invalid, i.e. when the requested list length
	 *             is longer than the original
	 */
	private <T> List<T> assign(List<T> list, int n, String errorVar) throws InvalidConfigurationException {
		// Not too long?
		if (n > list.size()) {
			throw new InvalidConfigurationException(errorVar + " was set to " + n
					+ " but the list from which to build it is only " + list.size() + " long.");
		}
		// Randomize the list first?
		if (beliefAssignment == AssignmentMethod.Randomly) {
			Collections.shuffle(list);
		}
		// List is 'ordered' so now assign the first n number of elements
		List<T> out = new ArrayList<T>();
		for (int i = 0; i < n; i++) {
			out.add(list.get(i));
		}
		return out;
	}

	@Override
	public String toString() {
		return g_d + " " + n_A + " " + n_R + " " + n_B_s + " " + n_O_s + " " + n_G_s + " " + n_O_r + " " + n_G_r + " "
				+ l + " " + n_G_nro + " " + n_B_ra + " " + n_B_nra + " " + beliefAssignment.name()
				+ " " + roleAssignment.name();
	}
}
