package org.aspic.inference;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.aspic.inference.parser.ParseException;
import org.aspic.inference.parser.PrologSyntax;
import org.aspic.inference.parser.TokenMgrError;
import org.aspic.inference.writers.GMLWriter;
import org.aspic.inference.writers.GraphvizWriter;
import org.aspic.inference.writers.SimpleGraphvizWriter;

/**
 * <p>An Engine has a knowledge base and can generate queries.  It
 * acts as a bridge by using the composed parser to convert
 * knowledge base strings and query expression strings to objects
 * that the underlying classes can use.</p>
 * 
 * @author mjs (matthew.south @ cancer.org.uk)
 *
 */
public class Engine implements Cloneable {
	/**
	 * Enumeration of the available Engine properties.  
	 * 
	 * It is assumed that each value is in turn represented 
	 * by another Enumeration.  
	 * 
	 * @author mjs (matthew.south @ cancer.org.uk)
	 */
	public enum Property { 
		/** Semantics of Reasoner used by Engine */
		SEMANTICS  { 
			Enum[] getValues() { 
				return Reasoner.values(); 
				} 
			void setValue(Engine eng, Enum value) {
				eng.reasoner = ((Reasoner) value).createReasoner();
			}
		},
		/** Automatically transpose strict rules **/
		TRANSPOSITION {
			Enum[] getValues() { 
				return OnOff.values(); 
				} 
			void setValue(Engine eng, Enum value) {
				eng.source.getKnowledgeBase().setUsingTransposition(value.equals(OnOff.ON) ? true : false);
			}			
		},
		/** Prevent defeasible Arguments rebutting strict Arguments **/
		RESTRICTED_REBUTTING {
			Enum[] getValues() { 
				return OnOff.values(); 
				} 
			void setValue(Engine eng, Enum value) {
				eng.source.setRebuttingRestricted((value.equals(OnOff.ON) ? true : false));
			}			
		},
		/** Valuation strategy for Arguments **/
		VALUATION {
			Enum[] getValues() { 
				return Valuator.values(); 
				} 
			void setValue(Engine eng, Enum value) {
				eng.source.setValuator((Valuator) value);
			}			
		};
		/**
		 * Abstract method for retrieving the supported values for
		 * this property.
		 * @return array of allowed values for a particular property.
		 */
		abstract Enum[] getValues();
		/**
		 * Abstract method for updating the engine appropriately 
		 * based on the passed value
		 * @param eng The engine to be updated
		 * @param value The new value for the property
		 */
		abstract void setValue(Engine eng, Enum value);
	}
	
	/**
	 * Enumeration for boolean Engine properties.  Used for transposition and "restricted rebutting".
	 * @author mjs (matthew.south @ cancer.org.uk)
	 *
	 */
	public enum OnOff { ON, OFF }
	
	/** Engine version */
	public final static String VERSION = "0.4.10";
	/** Engine name */
	public final static String NAME = "JavaAS";

	//private KnowledgeBase kb;
	private KnowledgeBaseSource source;
	private PrologSyntax parser; // TODO: Make this a factory?
	private AbstractReasoner reasoner;
	//private InferenceArgumentValuator valuator;
	//private boolean restrictedRebutting;

	private Map<Property, Enum> properties = new HashMap<Property, Enum>();
	
	public Engine(String knowledge) throws ParseException {
		parser = new PrologSyntax(new StringReader(knowledge));
		try {
			KnowledgeBase kb = parser.Knowledge();
			source = new KnowledgeBaseSource(kb);
			setDefaultProperties();
		} catch (TokenMgrError e) {
			throw new ParseException(e.getMessage());
		}
	}
		
	
	public Engine(KnowledgeBase kb) {
        parser = new PrologSyntax(new StringReader(""));
		source = new KnowledgeBaseSource(kb);
		setDefaultProperties();
	}
	
	/**
	 * Create new Query.
	 * @param expression list of terms to be queried
	 * @return new Query with result and proof
	 * @throws ParseException if the expression cannot be parsed
	 * @throws ReasonerException if the reasoner fails during Query generation
	 */
	public Query createQuery(String expression) throws ParseException, ReasonerException {
		parser.ReInit(new StringReader(expression));
		ConstantList query = parser.TermList();
		return new Query(query, reasoner, source, properties);
	}

	/**
	 * Creates new Query with a lower level parameter then <code>createQuery(String)</code>.
	 * @param query Constant to be queried
	 * @return new Query with result and proof
	 * @throws ParseException if the expression cannot be parsed
	 * @throws ReasonerException if the reasoner fails during Query generation
	 */
	public Query createQuery(Constant query) throws ParseException, ReasonerException {
		return new Query(query, reasoner, source, properties);
	}

	/**
	 * Creates new Query with a lower level parameter then <code>createQuery(String)</code>.
	 * @param query Constant to be queried
	 * @return new Query with result and proof
	 * @throws ParseException if the expression cannot be parsed
	 * @throws ReasonerException if the reasoner fails during Query generation
	 */
	public Query createQuery(ConstantList query) throws ParseException, ReasonerException {
		return new Query(query, reasoner, source, properties);
	}
	
	/**
	 * Get property value.
	 * @param property
	 * @return value of property
	 */
	public Enum getProperty(Property property) {
		return properties.get(property);
	}
	
	/**
	 * Set Property value.  If the passed value is not a supported
	 * value for the property (@see getSupportedValues) then an
	 * error is thrown.
	 * @param property property enum value
	 * @param value new value to be set
	 * @throws UnsupportedValueException if passed value is not supported by the property
	 */
	public void setProperty(Property property, Enum value) throws UnsupportedValueException {
		if (Arrays.asList(property.getValues()).contains(value)) {
			properties.put(property, value);
			property.setValue(this, value);
		} else {
			throw new UnsupportedValueException(value.toString() + " is an invalid value for " + property.toString());
		}
	}
	
	/**
	 * Get supported values for a particular property.
	 * @param property
	 * @return An array of Enum objects associated with the passed property
	 */
	public Enum[] getSupportedValues(Property property) {
		return property.getValues();
	}
	
	/**
	 * Get Knowledge Base
	 * @return Reference to embedded KnowledgeBase
	 * @deprecated Use getKnowledgeBaseSource instead
	 */
	public KnowledgeBase getKnowledgeBase() {
		return source.getKnowledgeBase();
	}
	
	/**
	 * An Exception that is thrown if a user attempts to set a property
	 * with an unsupported value.  Required because the supported values
	 * are not type safe.
	 * 
	 * @author mjs (matthew.south @ cancer.org.uk)
	 *
	 */
	public class UnsupportedValueException extends RuntimeException {
		public UnsupportedValueException(String message) {
			super(message);
		}
	}

	/**
	 * @deprecated Warning: leaks memory!
	 */
	public Object clone() {
		Engine o = null;
		try {
			o = (Engine) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		o.properties = (Map<Property, Enum>) ((HashMap<Property, Enum>)o.properties).clone();
		o.source = (KnowledgeBaseSource) o.source.clone();
		return o;
	}
	
	/**
	 * Getter for Engine's Argument Source.
	 * @return KnowledgeBaseSource.
	 */
	public KnowledgeBaseSource getKnowledgeBaseSource() {
		return source;
	}

	// assumes that knowledgebase has already been set.
	private void setDefaultProperties() {
		// set reasoner
		properties.put(Property.SEMANTICS, Reasoner.GROUNDED);
		reasoner = Reasoner.GROUNDED.createReasoner();
		// pass on KB's properties
		properties.put(Property.TRANSPOSITION, source.getKnowledgeBase().isUsingTransposition() ? OnOff.ON : OnOff.OFF);
		properties.put(Property.VALUATION, source.getValuator());
		properties.put(Property.RESTRICTED_REBUTTING, source.isRebuttingRestricted() ? OnOff.ON : OnOff.OFF);
	}
	
	/** 
	 * A logging formatter that only shows the message: nothing else
	 * 
	 * @author mjs (matthew.south @ cancer.org.uk)
	 */
	public static class MessageFormatter extends Formatter {
		public String format(LogRecord record) {
			return record.getMessage() + "\n";
		}
	}

}
