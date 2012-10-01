package org.aspic.inference;

import org.aspic.inference.writers.ReasonerWriter;

import java.io.Serializable;
import java.util.*;

/**
 * <p>A Query uses a Reasoner to evaluate a query expression.
 * The query expression is a list of (possibly ungrounded) Constants
 * representing a conjuntion of Terms.</p>
 * <p>A Query has a list of results which each consist of a grounded
 * expression and a flag indicating the status of that expression (defeated
 * or undefeated). A Query also has a proof which returns a list of 
 * discovered Arguments relevant to the whole query.</p>
 * <p>To create a Query, use the Engine object's <code>createQuery</code> method.</p>
 *
 * @author mjs (matthew.south @ cancer.org.uk)
 *
 */
public class Query implements Serializable {
	private Element expression;
	private List<RuleArgument> proof;
	private List<Result> results;
	private Map<Engine.Property, Enum> engineProperties; // eventually source will make take over this role.

	/**
	 * <p>Construct a query.  The query needs a question, the 
	 * <code>expression</code> and a way to answer that question which
	 * is provided by the <code>reasoner</code>.  The <code>reasoner</code>
	 * reasons over the knowledge base that is referenced by 
	 * the expression.  The engineProperties Map is passed in order
	 * to provide some context for an ArgumentationWriter when
	 * it's asked to explain the Query.</p> 
	 * @param expression the query's question
	 * @param reasoner the query's strategy for answering the question
	 * @param engineProperties the yes or nowider context of the query
	 */
	Query(Constant expression, AbstractReasoner reasoner, KnowledgeBaseSource source, Map<Engine.Property, Enum> engineProperties) {
		this.expression = expression;
		this.engineProperties = engineProperties;
		this.results = new LinkedList<Result>();
		proof = new LinkedList<RuleArgument>();
		Iterator<RuleArgument> matchIterator = source.argumentIterator(expression);
		while (matchIterator.hasNext()) {
			RuleArgument seed = matchIterator.next();
			Result result = new Result(seed.getClaim(), false);
			if (!proof.contains(seed)) proof.add(seed);
			// present testPair to the reasoner
			ReasonerPair reasoningResult = reasoner.evaluate(new ReasonerPair(new RuleArgumentList().cloneAndExtend(seed), new RuleArgumentList()), proof);
			// check result
			if (reasoningResult.getPRO().getArguments().size()>0) { 
				result.setUndefeated(true);
			}
			// add any new arguments, returned by the reasoner, into the proof.
			updateProof(reasoningResult.getPRO());
			updateProof(reasoningResult.getOPP());
			results.add(result);
		}
	}
	/**
	 * <p>Construct a query.  The query needs a question, the 
	 * <code>expression</code> and a way to answer that question which
	 * is provided by the <code>reasoner</code>.  The <code>reasoner</code>
	 * reasons over the knowledge base that is referenced by 
	 * the expression.  The engineProperties Map is passed in order
	 * to provide some context for an ArgumentationWriter when
	 * it's asked to explain the Query.</p> 
	 * @param list the query's question
	 * @param reasoner the query's strategy for answering the question
	 * @param engineProperties the wider context of the query
	 * */
	 Query(ConstantList list, AbstractReasoner reasoner, KnowledgeBaseSource source, Map<Engine.Property, Enum> engineProperties) {
		this.expression = list;
		this.engineProperties = engineProperties;
		this.results = new LinkedList<Result>();
		proof = new LinkedList<RuleArgument>();
		Iterator<RuleArgumentList> matchIterator = source.argumentIterator(list);
		while (matchIterator.hasNext()) {
			RuleArgumentList seedList = matchIterator.next();
			Result result = new Result(seedList.getClaims(), false);
			result.setUndefeated(true);
			Iterator<RuleArgument> seedIterator = seedList.getArguments().iterator();
			while (seedIterator.hasNext()) {
				RuleArgument seed = seedIterator.next();
				if (!proof.contains(seed)) proof.add(seed);
				// present testPair to the reasoner
				ReasonerPair reasoningResult = reasoner.evaluate(new ReasonerPair(new RuleArgumentList().cloneAndExtend(seed), new RuleArgumentList()), proof);
				// check result
				if (reasoningResult.getPRO().getArguments().size()==0) { 
					result.setUndefeated(false);
				}
				// add any new arguments, returned by the reasoner, into the proof.
				updateProof(reasoningResult.getPRO());
				updateProof(reasoningResult.getOPP());
			}
			results.add(result);
		}
	}
	
	/**
	 * A list of results - one for each different argument that could be develped whose claim
	 * is a match to the query expression.
	 * 
	 * @return list of results.
	 */
	public List<Result> getResults() {
		return results;
	}
	
	/**
	 * The proof for a Query is the Argument network used to resolve status of the query matches.
	 * @return a List of Main Arguments used to resolve the status of the query expression matches.  
	 */
	public List<RuleArgument> getProof() {
		return proof;
	}
	
	/**
	 * A way for an ArgumentWriter to see the context of the query.
	 * @return The properties of the engine that generated this query.
	 */
	public Map<Engine.Property, Enum> getEngineProperties() {
		return engineProperties;
	}
	
	/**
	 * A way to inspect the Query's expression.
	 * @return The query's expression.
	 */
	public Element getExpression() {
		return expression;
	}
	
	/**
	 * A way for an ArgumentWriter to see the context of the query.
	 * @return The argument source for this query.
	public ArgumentSource<Constant, Double> getSource() {
		return source;
	}
	 */
	
	/**
	 * Accept an ArgumentWriter object so that a query can be
	 * rendered in more than one way without needing multiple 
	 * writePlainText, writeHTML etc methods.
	 * @param writer a particular ArgumentWriter
	 */
	public void write(ReasonerWriter writer) {
		writer.write(this);
	}
	
	private void updateProof(RuleArgumentList list) {
		// TODO: check that Argument isnt already in proof.
		Iterator<RuleArgument> argumentIterator = list.getArguments().iterator();
		while (argumentIterator.hasNext()) {
			RuleArgument candidate = argumentIterator.next();
			if (!proof.contains(candidate)) proof.add(candidate);
		}
	}
}
