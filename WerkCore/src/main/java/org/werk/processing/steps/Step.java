package org.werk.processing.steps;

import org.werk.data.StepPOJO;
import org.werk.processing.jobs.Job;
import org.werk.processing.parameters.Parameter;

public interface Step extends StepPOJO {
	Job getJob();
	//String getStepTypeName();
	
	//--------------------------------------------
	
	//long getExecutionCount();
	long incrementExecutionCount();
	
	//Map<String, Parameter> getStepParameters();
	Parameter getStepParameter(String parameterName);
	Parameter removeStepParameter(String parameterName);
	void putStepParameter(String parameterName, Parameter parameter);
	
	//--------------------------------------------
	
	//List<String> getProcessingLog();
	void appendToProcessingLog(String message);
	
	StepExecutionResult appendToProcessingLog(StepExecutionResult record);
	StepExecutionResult appendToProcessingLog(StepExecutionResult record, String message);
	
	Transition appendToProcessingLog(Transition transition);
	Transition appendToProcessingLog(Transition transition, String message);

	//--------------------------------------------
	
	StepExec getStepExec();
	StepTransitioner getStepTransitioner();
}
