package org.aspic.inference.writers;


import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import org.aspic.inference.*;
/**
 * <p>A writer for yEd.  This writer builds a full graph 
 * which can be accessed via the toString() method.  Use this 
 * writer to build a GML graph around a particular query.</p>
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
public class GMLWriter implements ReasonerWriter {

	private StringBuffer graph = null; // graph that's currently in progress
	//private String group = null;  // Argument trees are drawn in a subgraph identified by group
	private List<String> knownArgs = new ArrayList<String>();
	private StringBuffer interactions; // argument interactions are drawn after the cluster is closed.
	
	private int id; 
	private Map<Integer, Integer> iNodes = new HashMap<Integer, Integer>();
	private Map<Integer, Integer> sNodes = new HashMap<Integer, Integer>();
	//private int gid;
	
	public GMLWriter() {
		resetDocument();
	}
	
	public void write(Query query) {
		graph.append("\tlabel\t\"" + query.getExpression().inspect() + ".\"\n");
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
		// now draw your arguments
		itr = query.getProof().iterator();
		while (itr.hasNext()) {
			RuleArgument argument = itr.next();
			//gid = drawNode(argument.getName(), "rectangle", false);
		    argument.write(this);
		}
		// now draw your interactions
		itr = query.getProof().iterator();
		while (itr.hasNext()) {
			drawInteractions(itr.next());
		}
	}

	public void write(RuleArgument argument) {
		// draw claim
		iNodes.put(argument.getNumber(), drawNode(argument.getClaim().inspect() + ".\n" + argument.getName() + ": " + argument.getModifier()));
		// draw sub-arguments
		if (argument.getSubArgumentList().getArguments().size()>0) {;
			sNodes.put(argument.getNumber(), drawNode("dMP", "ellipse"));
			drawEdge(sNodes.get(argument.getNumber()), iNodes.get(argument.getNumber()));
			// draw sub nodes
			Iterator<RuleArgument> itr = argument.getSubArgumentList().getArguments().iterator(); // subArgumentIterator includes this Argument itself and the makes an infinite loop.
			while (itr.hasNext()) {
				RuleArgument subArg = itr.next();
				subArg.write(this);
				drawEdge(iNodes.get(subArg.getNumber()), sNodes.get(argument.getNumber()));
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
		graph.append("Creator	\"" + Engine.NAME + "\"\n");
		graph.append("Version	\"" + Engine.VERSION + "\"\n");
		graph.append("graph\n[\n");
		graph.append("\thierarchic\t1\n");
		graph.append("\tdirected\t1\n");
		interactions = new StringBuffer();
		iNodes.clear();
		sNodes.clear();
	}
	
	public String toString() {
		return graph.toString() + interactions.toString() + "]";
	}
	
	private int drawNode(String label) {
		return drawNode(label, "rectangle");
	}

	private int drawNode(String label, String shape) {
		return drawNode(label, shape, true);
	}
	
	private int drawNode(String label, String shape, boolean drawGID) {
		// calculate length and width of node, based on label
		String[] lines = label.split("\\n");
		Arrays.sort(lines, new Comparator<String>() {
			public int compare(String o1, String o2) {
				return (o1.length() > o2.length()) ? -1 : ((o1.length()<o2.length()) ? 1 : 0);
			}
		});
		int width = lines[0].length()*8;
		int height = 10 + (lines.length * 15);
		
		// open node
		graph.append("\tnode\n\t[\n");
		// draw argument details
		graph.append("\t\tid\t" + id + "\n");
		if (label.endsWith("\n")) {
			graph.append("\t\tlabel\t\"" + label.substring(0, label.length()-1) + "\"\n");
		} else {
			graph.append("\t\tlabel\t\"" + label + "\"\n");			
		}
		// close node
		graph.append("\t\tgraphics\n");
		graph.append("\t\t[\n");
		graph.append("\t\t\tw\t" + width + "\n");
		graph.append("\t\t\th\t" + height + "\n");
		graph.append("\t\t\ttype\t\"" + shape + "\"\n");
		graph.append("\t\t]\n");
//		graph.append("\t\tLabelGraphics\n\t\t[\n\t\t\talignment\t\"left\"\n\t\t]\n");
/*		if (drawGID) {
			graph.append("\t\tgid\t" + gid + "\n");
		} else {
			graph.append("\t\tisGroup\t1\n");
		}*/
		graph.append("\t]\n");
		return id++;
	}
	
	private void drawEdge(Integer source, Integer target) {
		drawEdge(source, target, "#000000");
	}

	private void drawEdge(Integer source, Integer target, String rgb) {
		interactions.append("\tedge\n");
		interactions.append("\t[\n");
		interactions.append("\t\tsource\t" + source + "\n");
		interactions.append("\t\ttarget\t" + target + "\n");
		interactions.append("\t\tgraphics\n");
		interactions.append("\t\t[\n");
		interactions.append("\t\t\tfill\t\"" + rgb + "\"\n");
		interactions.append("\t\t\ttargetArrow\t\"standard\"\n");
		interactions.append("\t\t]\n");
		interactions.append("\t]\n");
	}
	
	private void drawInteractions(RuleArgument argument) {
		// winning interactions
		Iterator<RuleArgument> rebutIterator = argument.getSuccessfulAttackerCache().iterator();
		while (rebutIterator.hasNext()) {
			// add attack and defeat interactions
			RuleArgument rebutter = rebutIterator.next();
			if (knownArgs.contains(rebutter.getName())) {
				// Draw successful rebuts and defeats
				String color = (!argument.isMainArgument() && argument.getTopRule().getKnowledgeBase().isRuleName(argument.getTopRule().getConsequent())) ? "#0000FF" : "#FFFF00";
				Integer target = (!argument.isMainArgument() && argument.getTopRule().getKnowledgeBase().isRuleName(argument.getTopRule().getConsequent())) ? sNodes.get(argument.getParentNumber()) : iNodes.get(argument.getNumber());
				drawEdge(iNodes.get(rebutter.getNumber()), target, "#FF0000");
				drawEdge(iNodes.get(rebutter.getNumber()), target, color);
			}
		}
		// losing interactions
		Iterator<RuleArgument> counterIterator = argument.getUnsuccessfulAttackerCache().iterator();
		while (counterIterator.hasNext()) {
			// add attack interaction
			RuleArgument counter = counterIterator.next();
			if (knownArgs.contains(counter.getName())) {
				// draw unsuccessful rebuts and undercuts
				String color = (!argument.isMainArgument() && argument.getTopRule().getKnowledgeBase().isRuleName(argument.getTopRule().getConsequent())) ? "#0000FF" : "#FFFF00";
				Integer target = (!argument.isMainArgument() && argument.getTopRule().getKnowledgeBase().isRuleName(argument.getTopRule().getConsequent())) ? sNodes.get(argument.getParentNumber()) : iNodes.get(argument.getNumber());
				drawEdge(iNodes.get(counter.getNumber()), target, color);
			}		
		}
		// draw sub-argument interactions
		if (argument.getSubArgumentList().getArguments().size()>0) {;
			Iterator<RuleArgument> itr = argument.getSubArgumentList().getArguments().iterator(); // subArgumentIterator includes this Argument itself and the makes an infinite loop.
			while (itr.hasNext()) {
				drawInteractions(itr.next());
			}		
		}	

	}
}
