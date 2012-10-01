package nl.uu.cs.arg.platform.local;

import nl.uu.cs.arg.shared.Agent;

/**
 * 
 * Enumeration of available local running agents which are implementations  
 * of {@link Agent}. New agent implementations should be registered here. 
 * A factory is used to instantiate the agents.
 *  
 * @author erickok
 *
 */
public enum LocalAgent {

	InactiveAgent {
		@Override public Agent createAgent() { return new InactiveAgent(); }
		@Override public Agent createAgent(AgentXmlData xmlDataFile) { return new InactiveAgent(xmlDataFile); }		
	},
	BDIAgent {
		@Override public Agent createAgent() { return new BDIAgent(); }
		@Override public Agent createAgent(AgentXmlData xmlDataFile) { return new BDIAgent(xmlDataFile); }	
	},
	NonArguingAgent {
		@Override public Agent createAgent() { return new NonArguingAgent(); }
		@Override public Agent createAgent(AgentXmlData xmlDataFile) { return new NonArguingAgent(xmlDataFile); }	
	};
	
	public abstract Agent createAgent();
	public abstract Agent createAgent(AgentXmlData xmlDataFile);
	
}
