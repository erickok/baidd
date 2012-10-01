package nl.uu.cs.arg.platform.local;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aspic.inference.Constant;

public interface StrategyExposer {

	public List<ValuedOption> getAllOptions();

	public int getInitialBeliefsCount();

	public int getPrivateBeliefsCount(Set<Constant> publicbeliefs);

	public Map<String, Object> getStategyProperties();
	
}
