package org.werk.processing.jobs;

import java.util.List;
import java.util.Map;

import org.werk.processing.parameters.Parameter;
import org.werk.processing.steps.Step;

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
