package org.werk.steps;

import java.util.List;
import java.util.Map;

import org.werk.jobs.Job;
import org.werk.parameters.interfaces.Parameter;

public interface Step {
	Job getJob();
	String getStepTypeName();
	
	//--------------------------------------------
	
	long getExecutionCount();
	
	Map<String, Parameter> getStepParameters();
	Parameter getStepParameter(String parameterName);
	Parameter removeStepParameter(String parameterName);
	void putStepParameter(String parameterName, Parameter parameter);
	
	//--------------------------------------------
	
	List<String> getProcessingLog();
	void appendToProcessingLog(String message);
	
	StepExecutionResult appendToProcessingLog(StepExecutionResult record);
	StepExecutionResult appendToProcessingLog(StepExecutionResult record, String message);
	
	TransitionResult appendToProcessingLog(TransitionResult transitionResult);
	TransitionResult appendToProcessingLog(TransitionResult transitionResult, String message);

	//--------------------------------------------
	
	StepExec getStepExec();
	StepTransitioner getStepTransitioner();
}
