package org.aspic.inference.writers;


import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import org.aspic.inference.*;
/**
 * <p>A writer for DOT.  This writer builds a full graphviz graph 
 * which can be accessed via the toString() method.  Use this 
 * writer to build a DOT graph around a particular query.</p>
 * <pre>
 * Engine eng = new Engine("a.~a.");
 * Query query = eng.createQuery("a.");
 * GraphvizWriter writer = new GraphvizWriter();
 * query.write(writer);
 * System.out.println(writer.toString());
 * </pre>
 *
 * @author mjs (matthew.south@cancer.org.uk)
 */
public class GraphvizWriter implements ReasonerWriter {

	private StringBuffer graph = null; // graph that's currently in progress
	private String group = null;  // Argument trees are drawn in a subgraph identified by group
	private List<String> knownArgs = new ArrayList<String>();
	private StringBuffer interactions; // argument interactions are drawn after the cluster is closed.
	
	public GraphvizWriter() {
		resetDocument();
	}
	
	public void write(Query query) {
		// start by making a list of all relevant arguments, so we can exclude irrelevant arguments later.
		knownArgs = new ArrayList<String>();
		Iterator<RuleArgument> itr = query.getProof().iterator();
		while (itr.hasNext()) {
			RuleArgument arg = itr.next();
			Iterator<RuleArgument> itrSub = arg.subArgumentIterator();
			while (itrSub.hasNext()) { 
				knownArgs.add(itrSub.next().getName()); 
			}
		}
		// now draw your proof
		itr = query.getProof().iterator();
		while (itr.hasNext()) {
			RuleArgument arg = itr.next();
			/* The proof contains main arguments relevant to the query
			 * Draw a cluster (subgraph) and then draw then main argument and it's
			 * sub-arguments inside it. Collect interactions along the way, 
			 * and then draw them outside the cluster.
			 */ 
		    //String color = (!arg.getStatus().equals(Argument.Status.DEFEATED)) ? "#00CC00" : "#CC0000" ;
		    String fillcolor = (arg.getStatus()!=null) ? (arg.getStatus().equals(RuleArgument.Status.UNDEFEATED)) ? "#CCFFCC" : "#FFCCCC" : "#E0E0E0" ;
		    graph.append("\n  subgraph cluster" + arg.getNumber() + " {\n");
		    graph.append("    label     = \"" + arg.getName() + "\"\n") ;
		    graph.append("    fillcolor = \"" + fillcolor + "\"\n") ;
		    //graph.append("   color     = \"" + color + "\"\n") ;
		    graph.append("    style     = \"filled\"\n") ;
		    group = "cluster" + Integer.toString(arg.getNumber());
		    interactions = new StringBuffer();
		    arg.write(this);
		    graph.append("  } // end of " + arg.getName()+"\n\n");
		    graph.append(interactions);
		}
	}

	public void write(RuleArgument argument) {
		// draw claim
		graph.append("    " + argument.getName() + "  [shape=\"record\",label=\"{");
		if (!argument.isMainArgument() && argument.getTopRule().getKnowledgeBase().isRuleName(argument.getTopRule().getConsequent())) {
			Rule rule = argument.getTopRule().getKnowledgeBase().getRuleFromName(argument.getTopRule().getConsequent());
//			draw rule, but without the DOB.
			graph.append((rule.getName()!=null ? "[" + rule.getName().inspect() + "] " : "") + rule.getConsequent().inspect().replace("<", "\\<").replace(">", "\\>") + ((rule.getAntecedent()!=null && rule.getAntecedent().size()>0) ? " \\<- " + rule.getAntecedent().inspect().replace("<", "\\<").replace(">", "\\>") : ""));

		} else {
			graph.append(argument.getClaim().inspect().replace("<", "\\<").replace(">", "\\>"));			
		}
		graph.append("|{" + argument.getName() + "|" + argument.getModifier() + "}");
		//graph.append((argument.getStatus()==null) ? "" : "|{" + argument.getStatus() + "}");
		graph.append("}\",group=" + group + "];\n");
		
		// draw sub-arguments
		if (argument.getSubArgumentList().getArguments().size()>0) {;
			// draw inference node
			graph.append("    " + argument.getName() + "s  [label=\"(d)MP\", group=" + group + "]\n");
			graph.append("    " + argument.getName() + "s -> " + argument.getName() + "\n");
			// draw sub nodes
			Iterator<RuleArgument> itr = argument.getSubArgumentList().getArguments().iterator(); // subArgumentIterator includes this Argument itself and the makes an infinite loop.
			while (itr.hasNext()) {
				RuleArgument subArg = itr.next();
				graph.append("    " + subArg.getName() + " -> " + argument.getName() + "s\n");				
				subArg.write(this);
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
				String color = (!argument.isMainArgument() && argument.getTopRule().getKnowledgeBase().isRuleName(argument.getTopRule().getConsequent())) ? "darkslateblue" : "darkorange";
				String target = (!argument.isMainArgument() && argument.getTopRule().getKnowledgeBase().isRuleName(argument.getTopRule().getConsequent())) ? "Arg" + argument.getParentNumber() + "s" : argument.getName();
				interactions.append("  " + rebutter.getName() + " -> " + target + " [color=red];\n");
				interactions.append("  " + rebutter.getName() + " -> " + target + " [color=" + color + "];\n");					
			}
		}
		// losing interactions
		Iterator<RuleArgument> counterIterator = argument.getUnsuccessfulAttackerCache().iterator();
		while (counterIterator.hasNext()) {
			// add attack interaction
			RuleArgument counter = counterIterator.next();
			if (knownArgs.contains(counter.getName())) {
				// draw unsuccessful rebuts and undercuts
				String color = (!argument.isMainArgument() && argument.getTopRule().getKnowledgeBase().isRuleName(argument.getTopRule().getConsequent())) ? "darkslateblue" : "darkorange";
				String target = (!argument.isMainArgument() && argument.getTopRule().getKnowledgeBase().isRuleName(argument.getTopRule().getConsequent())) ? "Arg" + argument.getParentNumber() + "s" : argument.getName();
				interactions.append("  " + counter.getName() + " -> " + target + " [color=" + color + "];\n");
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
