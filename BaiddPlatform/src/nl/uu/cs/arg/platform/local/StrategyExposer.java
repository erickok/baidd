package nl.uu.cs.arg.platform.local;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aspic.inference.Constant;

public interface StrategyExposer {

	List<ValuedOption> getAllOptions();

	Set<Constant> getInitialBeliefs();

	Map<String, Object> getStategyProperties();

	
}
