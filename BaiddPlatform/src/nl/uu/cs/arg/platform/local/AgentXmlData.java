package nl.uu.cs.arg.platform.local;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import nl.uu.cs.arg.shared.dialogue.Goal;

import org.aspic.inference.KnowledgeBase;
import org.aspic.inference.Rule;
import org.aspic.inference.parser.ParseException;
import org.aspic.inference.parser.PrologSyntax;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Used to read an XML-based file with initial data to load
 * into an agent. Can contain initial beliefs and goals.
 * 
 * @author erickok
 *
 */
public class AgentXmlData {

	private static final String XML_AGENT = "baidd-agent";
	private static final String XML_BELIEFBASE = "beliefbase";
	private static final String XML_OPTIONS = "options";
	private static final String XML_OPTION = "option";
	private static final String XML_GOAL = "goal";
	private static final String XML_HIDDENGOALS = "hidden-goals";
	private static final String XML_PUBLICGOALS = "public-goals";
	private static final String XML_PROPERTIES = "properties";
	private static final String XML_PROPERTY = "property";
	private static final String XML_NAME = "name";
	private static final String XML_TYPE = "type";
	
	private String name;
	private KnowledgeBase beliefBase;
	private List<Rule> options;
	private List<Goal> hiddenGoals;
	private List<Goal> publicGoals;
	private Map<String, Object> properties;
	
	/**
	 * Takes a filename and parses its agent specification defined as XML
	 * @param xmlFile The XML file to read
	 * @return A data structure with read and parsed belief base and goals
	 * @throws ParserConfigurationException Exception on configuring the XML parser
	 * @throws IOException Exception on reading the input file
	 * @throws SAXException Exception on parsing the XML structure
	 * @throws ParseException Exception while parsing the beliefbase into Prolog-syntax knowledge
	 */
	public static AgentXmlData loadAgentDataFromXml(String name, File xmlFile) throws SAXException, IOException, ParserConfigurationException, ParseException {

		// Read the XML file using a DOM parser
		DocumentBuilderFactory xmlDocFactory = DocumentBuilderFactory.newInstance();
		//xmlDocFactory.setCoalescing(true);
		xmlDocFactory.setIgnoringComments(true);
		//xmlDocFactory.setIgnoringElementContentWhitespace(true);
		Document xmlDoc = xmlDocFactory.newDocumentBuilder().parse(xmlFile);
		PrologSyntax prologSyntax = new PrologSyntax(new StringReader("")); // A reuseable Prolog syntax parser
		
		// Read the <beliefbase> tag
		NodeList bbTags = xmlDoc.getElementsByTagName(XML_BELIEFBASE);
		KnowledgeBase beliefbase = null;
		if (bbTags != null && bbTags.getLength() > 0 && bbTags.item(0).getChildNodes().getLength() > 0) {
			for (int i = 0; i < bbTags.item(0).getChildNodes().getLength(); i++) {
				Node node = bbTags.item(0).getChildNodes().item(i);
				if (node != null && node.getTextContent() != null) {
					// Read the string contents into a parsed KnowledgeBase
					String content = node.getTextContent().trim();
					if (!content.equals("")) {
						prologSyntax.ReInit(new StringReader(content));
						beliefbase = prologSyntax.Knowledge();
					}
				}
			}
		}
		if (beliefbase == null) {
			beliefbase = new KnowledgeBase();
		}
		
		// Read the <options> tag
		NodeList optTags = xmlDoc.getElementsByTagName(XML_OPTIONS);
		List<Rule> options = new ArrayList<Rule>();
		if (optTags != null && optTags.getLength() > 0) {
			NodeList optTag = optTags.item(0).getChildNodes();
			if (optTag != null && optTag.getLength() > 0) {
				for (int i = 0; i < optTag.getLength(); i++) {
					// Read this <option> tag's contents as a parsed Term and wrap it in a (belief base) Rule
					String optText = optTag.item(i).getTextContent().trim();
					if (optText != null && !optText.equals("")) {
						prologSyntax.ReInit(new StringReader(optText));
						options.add(new Rule(prologSyntax.Term()));
					}
				}
			}
		}

		// Read the <hidden-goals> tag
		NodeList hgsTags = xmlDoc.getElementsByTagName(XML_HIDDENGOALS);
		List<Goal> hiddenGoals = parseGoalsTag(prologSyntax, hgsTags);

		// Read the <public-goals> tag
		NodeList pgsTags = xmlDoc.getElementsByTagName(XML_PUBLICGOALS);
		List<Goal> publicGoals = parseGoalsTag(prologSyntax, pgsTags);

		// Read the <properties> tag
		NodeList propTags = xmlDoc.getElementsByTagName(XML_PROPERTIES);
		Map<String, Object> properties = new HashMap<String, Object>();
		if (propTags != null && propTags.getLength() > 0) {
			NodeList propTag = propTags.item(0).getChildNodes();
			if (propTag != null && propTag.getLength() > 0) {
				for (int i = 0; i < propTag.getLength(); i++) {
					// Properties should have the format <property name="{name}" type="{type}">{value}</property>
					// {name} is the property name (as String)
					// {type} is the data type for the property value (used to parse the value)
					// {value} is the actual property value
					String optText = propTag.item(i).getTextContent().trim();
					if (optText != null && !optText.equals("") && propTag.item(i).getAttributes().getLength() > 0) {
						String optName = propTag.item(i).getAttributes().getNamedItem(XML_NAME).getNodeValue();
						String optType = propTag.item(i).getAttributes().getNamedItem(XML_TYPE).getNodeValue();
						if (optType != null && optType.toLowerCase().equals("boolean")) {
							properties.put(optName, Boolean.parseBoolean(optText));
						} else if (optType != null && optType.toLowerCase().equals("integer")) {
							properties.put(optName, Integer.parseInt(optText));
						} else {
							properties.put(optName, optText);
						}
					}
				}
			}
		}

		return new AgentXmlData(name, beliefbase, options, hiddenGoals, publicGoals, properties);
	}

	private static List<Goal> parseGoalsTag(PrologSyntax prologSyntax, NodeList hgsTags) throws ParseException {
		// Goals have the syntax 'goal.[ utility]'
		List<Goal> goals = new ArrayList<Goal>();
		if (hgsTags != null && hgsTags.getLength() > 0) {
			NodeList hgTags = hgsTags.item(0).getChildNodes();
			if (hgTags != null && hgTags.getLength() > 0) {
				for (int i = 0; i < hgTags.getLength(); i++) {
					// Read this <goal> tag as a parsed Term, possibly with an added utility value
					String hgText = hgTags.item(i).getTextContent().trim();
					if (hgText != null && !hgText.equals("")) {
						String[] hgElements = hgText.split(" ");
						prologSyntax.ReInit(new StringReader(hgElements[0]));
						if (hgElements.length > 1) {
							goals.add(new ValuedGoal(prologSyntax.Term(), Integer.parseInt(hgElements[1])));
						} else {
							goals.add(new Goal(prologSyntax.Term()));
						}
					}
				}
			}
		}
		return goals;
	}
	
	public void saveAgentDataToXml(File agentFile) throws ParserConfigurationException, TransformerConfigurationException, TransformerException, TransformerFactoryConfigurationError, IOException {
		
		// Create an XML file using a DOM document builder
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		
		// Create root
		Element root = doc.createElement(XML_AGENT);
		doc.appendChild(root);
		
		// Add beliefbase
		Element beliefsE = doc.createElement(XML_BELIEFBASE);
		beliefsE.appendChild(doc.createCDATASection(beliefBase.inspect(false, false)));
		root.appendChild(beliefsE);
		
		// Add options
		Element optionsE = doc.createElement(XML_OPTIONS);
		if (options != null) {
			for (Rule option : options) {
				Element optionE = doc.createElement(XML_OPTION);
				optionE.appendChild(doc.createTextNode(option.inspect()));
				optionsE.appendChild(optionE);
			}
		}
		root.appendChild(optionsE);
		
		// Add goals
		Element hiddenGoalsE = doc.createElement(XML_HIDDENGOALS);
		Element publicGoalsE = doc.createElement(XML_PUBLICGOALS);
		if (hiddenGoals != null) {
			for (Goal goal : hiddenGoals) {
				Element goalE = doc.createElement(XML_GOAL);
				goalE.appendChild(doc.createTextNode(goal.inspect()));
				hiddenGoalsE.appendChild(goalE);
			}
		}
		if (publicGoals != null) {
			for (Goal goal : publicGoals) {
				Element goalE = doc.createElement(XML_GOAL);
				goalE.appendChild(doc.createTextNode(goal.inspect()));
				publicGoalsE.appendChild(goalE);
			}
		}
		root.appendChild(hiddenGoalsE);
		root.appendChild(publicGoalsE);
		
		// Add properties
		Element propertiesE = doc.createElement(XML_PROPERTIES);
		if (properties != null) {
			for (Entry<String, Object> prop : properties.entrySet()) {
				Element propE = doc.createElement(XML_PROPERTY);
				propE.setAttribute(XML_NAME, prop.getKey());
				// TODO: Fix this for non-boolean properties
				propE.setAttribute(XML_TYPE, "boolean");
				propE.appendChild(doc.createTextNode(prop.getValue().toString()));
				propertiesE.appendChild(propE);
			}
		}
		root.appendChild(propertiesE);
		
		// Create the agent XML output file
		if (agentFile.exists()) {
			agentFile.delete();
		}
		agentFile.createNewFile();
		TransformerFactory.newInstance().newTransformer().transform(new DOMSource(doc), new StreamResult(agentFile));
		
	}
	
	public AgentXmlData(String name, KnowledgeBase beliefBase, List<Rule> options, List<Goal> hiddenGoals, List<Goal> publicGoals, Map<String, Object> properties) {
		this.name = name;
		this.beliefBase = beliefBase;
		this.options = options;
		this.hiddenGoals = hiddenGoals;
		this.publicGoals = publicGoals;
		this.properties = properties;
	}

	/**
	 * @return The initial name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return The raw initial belief base
	 */
	public KnowledgeBase getBeliefBase() {
		return beliefBase;
	}

	/**
	 * @return The list of initially known options
	 */
	public List<Rule> getOptions() {
		return options;
	}

	/**
	 * @return The list of initial hidden goals
	 */
	public List<Goal> getHiddenGoals() {
		return hiddenGoals;
	}

	/**
	 * @return The list of initial public goals
	 */
	public List<Goal> getPublicGoals() {
		return publicGoals;
	}

	/**
	 * @return The list of raw properties (with typed values)
	 */
	public Map<String, Object> getRawProperties() {
		return properties;
	}
	
}
