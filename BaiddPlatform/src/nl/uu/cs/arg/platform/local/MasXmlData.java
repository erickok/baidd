package nl.uu.cs.arg.platform.local;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import nl.uu.cs.arg.shared.Agent;
import nl.uu.cs.arg.shared.dialogue.Goal;
import nl.uu.cs.arg.shared.dialogue.protocol.DeliberationRule;
import nl.uu.cs.arg.shared.dialogue.protocol.OutcomeSelectionRule;
import nl.uu.cs.arg.shared.dialogue.protocol.TerminationRule;

import org.aspic.inference.Constant;
import org.aspic.inference.Term;
import org.aspic.inference.Variable;
import org.aspic.inference.parser.ParseException;
import org.aspic.inference.parser.PrologSyntax;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class MasXmlData {

	private static final String FILEEXT_AGENT = ".agent";
	private static final String XML_MAS = "baidd-mas";
	private static final String XML_TOPIC = "topic";
	private static final String XML_TOPICGOAL = "goal";
	private static final String XML_PROTOCOL = "protocol";
	private static final String XML_RULE = "rule";
	private static final String XML_OUTCOME = "outcome-rule";
	private static final String XML_DELIBERATION = "deliberation-rules";
	private static final String XML_TERMINATION = "termination-rules";
	private static final String XML_AGENTS = "agents";
	private static final String XML_AGENT = "agent";
	private static final String XML_NAME = "name";
	private static final String XML_TYPE = "type";
	private static final String XML_FILE = "file";

	/**
	 * The topic of this deliberation dialogue, which should be a {@link Term} with 
	 * one or more free {@link Variable}s.
	 */
	private final Term topic;
	
	/**
	 * The concrete (no free {@link Variable}s) mutual {@link Goal} for this 
	 * deliberation dialogue.
	 */
	private final Goal topicGoal;
	
	/**
	 * List of {@link Agent} instances as loaded form an XML specification. 
	 */
	private final List<Agent> localAgents;

	/**
	 * List of {@link AgentXmlData} instanced that contain the XML specifications of the loaded agents
	 */
	private final List<AgentXmlData> agentXmlDatas;
	
	/**
	 * List of the active {@link DeliberationRule}s on this platform
	 */
	private final List<DeliberationRule> deliberationRules;

	/**
	 * List of the active {@link TerminationRule}s on this platform
	 */
	private final List<TerminationRule> terminationRules;
	
	/**
	 * The {@link OutcomeSelectionRule} to use when determining a dialogue's winning proposals
	 */
	private final OutcomeSelectionRule outcomeSelectionRule;
	
	/**
	 * Takes a filename and parses the contents (the agents to start) defined in XML
	 * @param xmlFile The XML file to read
	 * @return A data structure with instantiated agents as defined in the MAS XML file
	 * @throws ParserConfigurationException Exception on configuring the XML parser
	 * @throws IOException Exception on reading the input file
	 * @throws SAXException Exception on parsing the XML structure
	 * @throws ParseException Exception while parsing the agent XML files into Prolog-syntax knowledge
	 */
	public static MasXmlData loadAgentDataFromXml(File xmlFile) throws SAXException, IOException, ParserConfigurationException, ParseException {
		
		// Read the XML file using a DOM parser
		DocumentBuilderFactory xmlDocFactory = DocumentBuilderFactory.newInstance();
		//xmlDocFactory.setCoalescing(true);
		xmlDocFactory.setIgnoringComments(true);
		xmlDocFactory.setIgnoringElementContentWhitespace(true);
		Document xmlDoc = xmlDocFactory.newDocumentBuilder().parse(xmlFile);
		PrologSyntax prologSyntax = new PrologSyntax(new StringReader("")); // A reusable Prolog syntax parser
		
		// Read the <topic> tag
		NodeList topicTags = xmlDoc.getElementsByTagName(XML_TOPIC);
		Constant topic = null;
		Goal topicGoal = null;
		if (topicTags != null && topicTags.getLength() > 0) {
			
			// Read the topic text
			Node topicTag = topicTags.item(0);
			String topicText = topicTag.getTextContent();
			if (topicText != null && !topicText.equals("")) {
				prologSyntax.ReInit(new StringReader(topicText));
				topic = prologSyntax.Term();
				
				// Read the topic goal, which is an attribute of the topic tag
				if (topicTag.hasAttributes()) {
					prologSyntax.ReInit(new StringReader(topicTag.getAttributes().getNamedItem(XML_TOPICGOAL).getTextContent()));
					topicGoal = new Goal(prologSyntax.Term());
				}
			}
		}

		// If no topic was specified: throw an exception (note that the topic goal is allow to be null, for now)
		if (topic == null) {
			throw new ParseException("No topic was specified in the XML file.");
		}
		
		// If the topic is not a Term or has no free Variables, it is not formed well: throw an exception
		Term topicTerm;
		try {
			// Is it a Term?
			topicTerm = (Term) topic;
			// Does it have elements at all?
			if (topicTerm.numberOfArgs() <= 0) {
				throw new ParseException("The topic as specified in the XML document has no variables. A topic should always be a term with free variables.");
			}
			// Does it have a free variable?
			int freeVarCount = 0;
			for (int i = 0; i < topicTerm.numberOfArgs(); i++) {
				if (topicTerm.getArg(i) instanceof Variable) { // Element's .isGrounded is not public :-(
					freeVarCount++;
				}
			}
			if (freeVarCount == 0) {
				throw new ParseException("The topic as specified in the XML document has no free variables. A topic should always be a term with free variables.");
			}
		} catch (ClassCastException e) {
			throw new ParseException("The topic as specified in the XML document is not a term but a constant. A topic should always be a term with free variables.");
		}
		
		// Read the protocol (outcome, deliberation and termination) rules
		OutcomeSelectionRule outcomeRule = OutcomeSelectionRule.FirstThatIsIn;
		List<DeliberationRule> deliberationRules = new ArrayList<DeliberationRule>();
		List<TerminationRule> terminationRules = new ArrayList<TerminationRule>();
		NodeList protocolTags = xmlDoc.getElementsByTagName(XML_PROTOCOL);
		if (protocolTags != null && protocolTags.getLength() > 0) {
			
			// The outcome rule is an attribute to the <protocol> tag
			Node protocolNode = protocolTags.item(0);
			if (protocolNode.hasAttributes()) {
				Node outcomeItem = protocolNode.getAttributes().getNamedItem(XML_OUTCOME);
				if (outcomeItem != null) {
					outcomeRule = OutcomeSelectionRule.valueOf(outcomeItem.getTextContent());
				}
			}
			
			// The <protocol> tag may have a <deliberation-rules> and a <termination-rules> tag
			NodeList rulesTags = protocolTags.item(0).getChildNodes();
			for (int i = 0; i < rulesTags.getLength(); i++) {
				Node ruleTag = rulesTags.item(i);
				
				// Depending on the tag name, add its child nodes, which are rules, to the correct rules list
				NodeList ruleNodes = ruleTag.getChildNodes();
				for (int j = 0; j < ruleNodes.getLength(); j++) {
					String ruleContent = ruleNodes.item(j).getTextContent().trim();
					
					if (ruleContent != null && !ruleContent.equals("")) {
						if (ruleTag.getNodeName() == XML_DELIBERATION) {
							deliberationRules.add
									(DeliberationRule.valueOf(ruleContent));
							
						} else if (ruleTag.getNodeName() == XML_TERMINATION) {
							terminationRules.add(
									TerminationRule.valueOf(ruleContent));
						}
					}
					
				}
				
			}
			
		}
		
		
		// Read all <agent> tags
		ArrayList<MasXmlAgentLine> agentLines = new ArrayList<MasXmlAgentLine>();
		NodeList agentTags = xmlDoc.getElementsByTagName(XML_AGENT);
		for (int i = 0; i < agentTags.getLength(); i++) {
			
			// Read the attributes of this <agent> tag
			NamedNodeMap agentAttributes = agentTags.item(i).getAttributes();
			if (agentAttributes != null && agentAttributes.getLength() > 0) {
				Node name = agentAttributes.getNamedItem(XML_NAME);
				Node type = agentAttributes.getNamedItem(XML_TYPE);
				Node file = agentAttributes.getNamedItem(XML_FILE);
				
				// Add the read <agent> line with the available attribute values
				agentLines.add(
						new MasXmlAgentLine(
								(name != null? name.getNodeValue(): null), 
								(type != null? type.getNodeValue(): null), 
								(file != null? file.getNodeValue(): null)));
				
			}
			
		}
		
		// Read a list local agent types and XML agent data filenames and instantiate the LocalAgents
		ArrayList<Agent> typedLocalAgents = new ArrayList<Agent>(); 
		ArrayList<AgentXmlData> xmlAgents = new ArrayList<AgentXmlData>();
		for (MasXmlAgentLine rawLocalAgent : agentLines) {
			
			// Read the agent XML specification file
			AgentXmlData xmlAgent = AgentXmlData.loadAgentDataFromXml(
					rawLocalAgent.getName(), 
					new File(xmlFile.getParent() + File.separator + rawLocalAgent.getFile()));
			xmlAgents.add(xmlAgent);
					
			// Instantiate a LocalAgent based on the type (the String is converted to an LocalAgent type)
			typedLocalAgents.add(LocalAgent.valueOf(rawLocalAgent.getType()).createAgent(xmlAgent));
			
		}
		
		return new MasXmlData(topicTerm, topicGoal, typedLocalAgents, xmlAgents, deliberationRules, terminationRules, outcomeRule);
	}
	
	public void saveMasDataToXml(File masFile, boolean writeAgents) throws ParserConfigurationException, IOException, TransformerConfigurationException, TransformerException, TransformerFactoryConfigurationError {

		// Create an XML file using a DOM document builder
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		
		// Create save directory and MAS output file
		File saveDir = masFile.getParentFile();
		if (!saveDir.exists()) {
			saveDir.mkdirs();
		}
		if (masFile.exists()) {
			masFile.delete();
		}
		masFile.createNewFile();
		
		// Add root node and topic with goal
		Element root = doc.createElement(XML_MAS);
		doc.appendChild(root);
		Element topicE = doc.createElement(XML_TOPIC);
		topicE.setAttribute(XML_TOPICGOAL, topicGoal.inspect());
		topicE.appendChild(doc.createTextNode(topic.inspect()));
		root.appendChild(topicE);
		
		// Add protocol rules
		Element protocol = doc.createElement(XML_PROTOCOL);
		Element deliberationRulesE = doc.createElement(XML_DELIBERATION);
		Element terminationRulesE = doc.createElement(XML_TERMINATION);
		for (DeliberationRule rule : deliberationRules) {
			Element ruleE = doc.createElement(XML_RULE);
			ruleE.appendChild(doc.createTextNode(rule.toString()));
			deliberationRulesE.appendChild(ruleE);
		}
		for (TerminationRule rule : terminationRules) {
			Element ruleE = doc.createElement(XML_RULE);
			ruleE.appendChild(doc.createTextNode(rule.toString()));
			terminationRulesE.appendChild(ruleE);
		}
		protocol.appendChild(deliberationRulesE);
		protocol.appendChild(terminationRulesE);
		root.appendChild(protocol);
		
		// Add agent definitions
		Element agentsE = doc.createElement(XML_AGENTS);
		for (Agent agent : localAgents) {
			Element agentE = doc.createElement(XML_AGENT);
			agentE.setAttribute(XML_NAME, agent.getName());
			agentE.setAttribute(XML_TYPE, agent.getClass().getSimpleName());
			agentE.setAttribute(XML_FILE, agent.getName() + FILEEXT_AGENT);
			agentsE.appendChild(agentE);
		}
		root.appendChild(agentsE);
		
		// Write MAS file
		TransformerFactory.newInstance().newTransformer().transform(new DOMSource(doc), new StreamResult(masFile));
		
		if (writeAgents) {
			for (AgentXmlData agent : agentXmlDatas) {
				agent.saveAgentDataToXml(new File(saveDir.toString() + File.separator + agent.getName() + FILEEXT_AGENT));
			}
		}
		
	}

	public MasXmlData(Term topic, Goal topicGoal, List<Agent> localAgents, List<AgentXmlData> agentXmlDatas, List<DeliberationRule> deliberationRules, List<TerminationRule> terminationRules, OutcomeSelectionRule outcomeSelectionRule) {
		this.topic = topic;
		this.topicGoal = topicGoal;
		this.localAgents = localAgents;
		this.agentXmlDatas = agentXmlDatas;
		this.deliberationRules = deliberationRules;
		this.terminationRules = terminationRules;
		this.outcomeSelectionRule = outcomeSelectionRule;
	}

	/**
	 * Return the dialogue topic as read from the XML file
	 * @return A {@link Term} with free {@link Variable}s that represents the dialogue topic
	 */
	public Term getTopic() {
		return this.topic;
	}
	
	/**
	 * Returns the mutual goal to respect in the deliberation dialogue, as read from the XML file
	 * @return A concrete (no free {@link Variable}s) mutual {@link Goal}
	 */
	public Goal getTopicGoal() {
		return this.topicGoal;
	}
	
	/**
	 * Returns the {@link Agent}s as read from the XML file
	 * @return The instantiated agents (possibly read from their own agent XML files)
	 */
	public List<Agent> getLocalAgents() {
		return localAgents;
	}
	
	/**
	 * Returns the list {@link AgentXmlData} objects that contains the XML specifications of the loaded agents
	 * @return
	 */
	public List<AgentXmlData> getAgentXmlDatas() {
		return agentXmlDatas;
	}

	/**
	 * Returns the deliberation rules to apply to the platform
	 * @return A list of {@link DeliberationRule}s
	 */
	public List<DeliberationRule> getDeliberationRules() {
		return this.deliberationRules;
	}

	/**
	 * Returns the termination rules to apply to the platform
	 * @return A list of {@link TerminationRule}s
	 */
	public List<TerminationRule> getTerminationRules() {
		return this.terminationRules;
	}
	
	/**
	 * Returns the rule how to select the winning proposal from a dialogue
	 * @return The dialogue outcome selection rule
	 */
	public OutcomeSelectionRule getOutcomeSelectionRule() {
		return this.outcomeSelectionRule;
	}
	
	/**
	 * A private class that contains a raw String tuple <name, type, file>
	 * as read from the MAS XML file.
	 * 
	 * @author erickok
	 *
	 */
	private static class MasXmlAgentLine {
		
		private String name;
		private String type;
		private String file;
		
		public MasXmlAgentLine(String name, String type, String file) {
			this.name = name;
			this.type = type;
			this.file = file;
		}
		
		public String getName() { return this.name; }
		public String getType() { return this.type; }
		public String getFile() { return this.file; }
		
	}
	
}
