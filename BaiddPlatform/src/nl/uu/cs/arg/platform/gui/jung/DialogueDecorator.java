package nl.uu.cs.arg.platform.gui.jung;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;

import nl.uu.cs.arg.shared.dialogue.Move;
import nl.uu.cs.arg.shared.dialogue.locutions.DeliberationLocution;
import nl.uu.cs.arg.shared.dialogue.locutions.Locution;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;

public class DialogueDecorator {

	public static class VertexLabeller extends ToStringLabeller<Move<? extends Locution>> {
		@Override
		public String transform(Move<? extends Locution> v) {
			return "<html>" + v.getLocution().toLogicString().replace("\n", "<br />").replace(" ", "&nbsp;");
			//return v.getLocution().toLogicString().replace("\n", "<br />");
		}
	}

	/*public static class VertexShaper implements Transformer<Move<? extends Locution>, Shape> {
		private Font rendererFont;
		private FontRenderContext frc;
		public VertexShaper(Font rendererFont, FontRenderContext frc) {
			this.rendererFont = rendererFont;
			this.frc = frc;
		}
		@Override
		public Shape transform(Move<? extends Locution> arg0) {
			Rectangle2D string = rendererFont.getStringBounds(arg0.getLocution().toLogicString(), frc);
			//return new Rectangle((int)string.getWidth() + 40, (int)string.getHeight());
			return new Rectangle(0-(((int)string.getWidth() + 40)/2), 0-((int)string.getHeight()/2), (int)string.getWidth() + 40, (int)string.getHeight());
		}		
	}*/

	/**
	 * Used as a delegate to ask for the dialogical state of some move
	 */
	public interface MoveStateEvaluator {
		public boolean evaluateMove(Move<? extends Locution> move);
	}
	
	public static class VertexPainter implements Transformer<Move<? extends Locution>, Paint> {
		private MoveStateEvaluator stateDelegate;
		public VertexPainter(MoveStateEvaluator stateDelegate) {
			this.stateDelegate = stateDelegate;
		}
		@Override
		public Paint transform(Move<? extends Locution> move) {
			if (move == null || move.getLocution() == null || !(move.getLocution() instanceof DeliberationLocution)) {
				return Color.black;
			}
			// For 'deliberation locutions', return the dialogical status within their proposal
			return (stateDelegate.evaluateMove(move)? Color.green: Color.red);
		}		
	}

	public static class VertexFillPainter implements Transformer<Move<? extends Locution>, Paint> {
		@Override
		public Paint transform(Move<? extends Locution> move) {
			return Color.white;
		}		
	}

	public static class VertexStroker implements Transformer<Move<? extends Locution>,Stroke> {
		private MoveStateEvaluator stateDelegate;
		private static Stroke dashedLine = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 4.0f, new float[] { 4.0f }, 0.0f);
		private static Stroke solidLine = new BasicStroke(1.0f);
		public VertexStroker(MoveStateEvaluator stateDelegate) {
			this.stateDelegate = stateDelegate;
		}
		@Override
		public Stroke transform(Move<? extends Locution> move) {
			if (move == null || move.getLocution() == null || !(move.getLocution() instanceof DeliberationLocution)) {
				return solidLine;
			}
			// For 'deliberation locutions', return the dialogical status within their proposal
			return (stateDelegate.evaluateMove(move)? solidLine: dashedLine);
		}	
	}

	public static class EdgeArrow implements Transformer<Move<? extends Locution>, Shape> {
		@Override
		public Shape transform(Move<? extends Locution> move) {
			return null;
		}		
	}

	public static class ToolTipLabeller extends ToStringLabeller<Move<? extends Locution>> {
		@Override
		public String transform(Move<? extends Locution> v) {
			return "<html>" + v.toLogicString().replace("\n", "<br />").replace(" ", "&nbsp;");
		}
	}
	
}
