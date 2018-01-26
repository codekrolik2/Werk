package org.werk.processing.steps;

import java.util.List;
import java.util.Map;

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
	Long getLongParameter(String parameterName);
	void putLongParameter(String parameterName, Long value);
	Double getDoubleParameter(String parameterName);
	void putDoubleParameter(String parameterName, Double value);
	Boolean getBoolParameter(String parameterName);
	void putBoolParameter(String parameterName, Boolean value);
	String getStringParameter(String parameterName);
	void putStringParameter(String parameterName, String value);
	Map<String, Parameter> getDictionaryParameter(String parameterName);
	void putDictionaryParameter(String parameterName, Map<String, Parameter> value);
	List<Parameter> getListParameter(String parameterName);
	void putListParameter(String parameterName, List<Parameter> value);
	
	//--------------------------------------------
	
	//List<String> getProcessingLog();
	void appendToProcessingLog(String message);
	
	ExecutionResult appendToProcessingLog(ExecutionResult record);
	ExecutionResult appendToProcessingLog(ExecutionResult record, String message);
	
	Transition appendToProcessingLog(Transition transition);
	Transition appendToProcessingLog(Transition transition, String message);

	//--------------------------------------------
	
	StepExec getStepExec();
	StepTransitioner getStepTransitioner();
}
