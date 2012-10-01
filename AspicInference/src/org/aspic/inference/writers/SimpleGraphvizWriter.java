package org.aspic.inference.writers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import org.aspic.inference.*;

/**
 * <p>A writer for DOT.  This writer builds a simplified graphviz graph 
 * which can be accessed via the toString() method.  Use this 
 * writer to build a DOT graph around a particular query.</p>
 * <pre>
 * Engine eng = new Engine("a.~a.");
 * Query query = eng.createQuery("a.");
 * GraphvizWriter writer = new GraphvizWriter();
 * query.write(writer);
 * System.out.println(writer.toString());
 * </pre>
 * The simplified graphviz graph just shows the relevant main arguments,
 * the defeat interactations, and (optionally) the attack relations.
 * 
 * @author mjs (matthew.south@cancer.org.uk)
 */
public class SimpleGraphvizWriter implements ReasonerWriter {

	private boolean showAttacks = false;
	private StringBuffer graph = null; // graph that's currently in progress
	private List<String> knownArgs = new ArrayList<String>(); // a list of all args listed in proof (so that we can exclude unused arguments)
	private Map<String, String> mainArg; // maps ArgName to associate Main Argument ArgName
	
	public SimpleGraphvizWriter() {
		resetDocument();
	}

	public SimpleGraphvizWriter(boolean showAttacks) {
		resetDocument();
		this.showAttacks = showAttacks;
	}
	
	public void write(Query query) {
		knownArgs = new ArrayList<String>();
		mainArg = new HashMap<String, String>();
		// start by making a list of all relevant arguments, so we can exclude irrelevant arguments later.		
		Iterator<RuleArgument> itr = query.getProof().iterator();
		while (itr.hasNext()) {
			RuleArgument arg = itr.next();
			Iterator<RuleArgument> itrSub = arg.subArgumentIterator();
			while (itrSub.hasNext()) {
				RuleArgument subArg = itrSub.next();
				knownArgs.add(subArg.getName());
				mainArg.put(subArg.getName(), arg.getName());
			}
		}
		// now draw your proof
		itr = query.getProof().iterator();
		while (itr.hasNext()) {
			itr.next().write(this);
		}
	}

	public void write(RuleArgument argument) {
		// draw claim
		if (argument.isMainArgument()) {
			graph.append("  " + argument.getName() + " [shape=\"record\",label=\"{");
			graph.append(argument.getClaim().inspect().replace("<", "\\<").replace(">", "\\>"));			
			graph.append("|{" + argument.getName() + "|" + argument.getModifier() + "}}\"");
			graph.append((argument.getStatus()==null) ? "" : ", style=\"filled\" fillcolor=\"" + ((argument.getStatus().equals(RuleArgument.Status.UNDEFEATED)) ? "#CCFFCC" : "#FFCCCC") + "\"");
			graph.append("];\n");
		}
		// draw sub-arguments
		if (argument.getSubArgumentList().getArguments().size()>0) {;
			// draw sub nodes
			Iterator<RuleArgument> itr = argument.getSubArgumentList().getArguments().iterator(); // subArgumentIterator includes this Argument itself and the makes an infinite loop.
			while (itr.hasNext()) {
				itr.next().write(this);
			}		
		}
		
		// draw interactions
		
		// winning interactions
		Iterator<RuleArgument> rebutIterator = argument.getSuccessfulAttackerCache().iterator();
		while (rebutIterator.hasNext()) {
			// add attack and defeat interactions
			RuleArgument rebutter = rebutIterator.next();
			if (knownArgs.contains(rebutter.getName())) {
				// Draw successful rebuts and defeats
				String target = (!argument.isMainArgument() && argument.getTopRule().getKnowledgeBase().isRuleName(argument.getTopRule().getConsequent())) ? mainArg.get("Arg" + argument.getParentNumber()) : mainArg.get(argument.getName());
				graph.append("  " + rebutter.getName() + " -> " + target + " [color=red];\n");
				if (showAttacks) {
					String color = (!argument.isMainArgument() && argument.getTopRule().getKnowledgeBase().isRuleName(argument.getTopRule().getConsequent())) ? "darkslateblue" : "darkorange";
					graph.append("  " + rebutter.getName() + " -> " + target + " [color=" + color + "];\n");					
				}
			}
		}
		// losing interactions
		if (showAttacks) {
			Iterator<RuleArgument> counterIterator = argument.getUnsuccessfulAttackerCache().iterator();
			while (counterIterator.hasNext()) {
				// add attack interaction
				RuleArgument counter = counterIterator.next();
				if (knownArgs.contains(counter.getName())) {
					// draw unsuccessful rebuts and undercuts
					String color = (!argument.isMainArgument() && argument.getTopRule().getKnowledgeBase().isRuleName(argument.getTopRule().getConsequent())) ? "darkslateblue" : "darkorange";
					String target = (!argument.isMainArgument() && argument.getTopRule().getKnowledgeBase().isRuleName(argument.getTopRule().getConsequent())) ? "Arg" + argument.getParentNumber() + "s" : argument.getName();
					graph.append("  " + counter.getName() + " -> " + target + " [color=" + color + "];\n");
				}		
			}
		}
	}

	public void write(RuleArgumentList argumentList) {
		/* This may be needed when and if ElementLists can be queries */
	}

	public void write(Substitution substitution) {
		/* TODO */
	}

	public void write(ReasonerPair reasonerPair) {
		/* Not needed for this writer */
	}
	
	public void resetDocument() {
		graph = new StringBuffer();
		graph.append("digraph G {\n");
		graph.append("  graph [\n    rankdir  = \"BT\"\n  ];\n");
		graph.append("  node [\n    fontname = \"Helvetica\"\n    fontsize = \"10\"\n  ];\n");
		graph.append("  edge [\n    fontname = \"Helvetica\"\n    fontsize = \"10\"\n    style    = \"setlinewidth(2)\"\n  ];\n");
	}
	
	public String toString() {
		return graph.toString() + "}";
	}
}
