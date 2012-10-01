package nl.uu.cs.arg.platform.gui.jung;

import java.util.HashSet;

import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.graph.Forest;

/**
 * This is a regular jung TreeLayout, but it allows resetting of the layout.
 * This is because of a bug in TreeLayout: nodes that are added after the 
 * first layout routine are not rendered at the correct location (but rather
 * top-left). See https://sourceforge.net/tracker/?func=detail&aid=2899213&group_id=73840&atid=539119
 * 
 * The fix is to call the .reset() after adding new vertexes/edges to the 
 * tree.
 * 
 * @author erickok
 *
 */
public class DynamicTreeLayout<V, E> extends TreeLayout<V, E> {

	public DynamicTreeLayout(Forest<V, E> g, int distx, int disty) {
		super(g, distx, disty);
	}

	public DynamicTreeLayout(Forest<V, E> g, int distx) {
		super(g, distx);
	}

	public DynamicTreeLayout(Forest<V, E> g) {
		super(g);
	}

	/*
	 * Destroys the previously constructed layout and generates a new one
	 */
	@Override
	public void reset() {
		alreadyDone = new HashSet<V>();
		buildTree();
	}

	
    
}
