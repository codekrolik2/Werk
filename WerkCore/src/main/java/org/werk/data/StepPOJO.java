package org.werk.data;

import java.util.List;
import java.util.Map;

import org.werk.processing.parameters.Parameter;
import org.werk.processing.steps.StepProcessingLogRecord;

public interface StepPOJO {
	String getStepTypeName();
	boolean isRollback();
	
	long getStepNumber();
	List<Long> getRollbackStepNumbers();
	long getExecutionCount();
	
	Map<String, Parameter> getStepParameters();
	List<StepProcessingLogRecord> getProcessingLog();
}
