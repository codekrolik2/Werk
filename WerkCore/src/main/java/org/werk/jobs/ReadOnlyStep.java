package org.werk.jobs;

import java.util.List;
import java.util.Map;

import org.werk.parameters.interfaces.Parameter;
import org.werk.steps.Step;

public interface ReadOnlyStep {
	String getStepTypeName();
	
	/**
	 * @return Read Only Map of step parameters
	 */
	Map<String, Parameter> getStepParameters();
	long getExecutionCount();
	
	List<String> getProcessingLog();
	
	void copyParametersTo(Step step);
}
