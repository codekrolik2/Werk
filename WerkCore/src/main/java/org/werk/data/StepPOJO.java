package org.werk.data;

import java.util.List;
import java.util.Map;

import org.werk.processing.parameters.Parameter;

public interface StepPOJO {
	String getStepTypeName();
	long getStepNumber();
	List<Long> getRollbackStepNumbers();
	long getExecutionCount();
	
	Map<String, Parameter> getStepParameters();
	List<String> getProcessingLog();
}
