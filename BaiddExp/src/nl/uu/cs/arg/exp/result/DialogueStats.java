package nl.uu.cs.arg.exp.result;

import java.util.Date;
import java.util.Map;

import nl.uu.cs.arg.platform.Settings;
import nl.uu.cs.arg.shared.Participant;
import nl.uu.cs.arg.shared.dialogue.Dialogue;

import org.aspic.inference.Constant;

public class DialogueStats {
	
	public final Integer id;
	public final Dialogue dialogue;
	public final int optionsCount;
	public final Integer configId;
	public final Date date;
	//public final Distribution distribution;
	public final Settings settings;

	public Map<Constant, Participant> publicbeliefs;
	public Map<Participant, Integer> otherbeliefsCount;
	public Map<Participant, Integer> ownbeliefsCount;
	public Map<Participant, Map<Constant, Integer>> utilities;
	public Map<String, Object> stratprops;
	public String agentStrategy;
	public int optionsInCount;

	public Integer e_moves;
	//public Float e_weakrelevance;
	public Float e_strongrelevance;
	public Float e_concealment;
	public Map<Constant, Integer> e_totalutility;
	public Float e_total_avg;
	public Integer e_total_o;
	public Float e_total_in_avg;
	public Constant o = null;
	public Map<Constant, Boolean> e_pareto;
	public Boolean e_pareto_o;

	public DialogueStats(Dialogue dialogue, int id, Date date, int configId, Settings settings, int optionsCount, String agentStrategy) {
		this.dialogue = dialogue;
		this.id = id;
		this.date = date;
		this.configId = configId;
		//this.distribution = distribution;
		this.settings = settings;
		this.optionsCount = optionsCount;
		this.agentStrategy = agentStrategy;
	}
	
}
