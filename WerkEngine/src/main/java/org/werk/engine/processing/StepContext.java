package org.werk.engine.processing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.werk.engine.WerkParameterException;
import org.werk.engine.processing.mapped.MappedParameter;
import org.werk.parameters.interfaces.Parameter;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class StepContext {
	long executionCount;
	Map<String, Parameter> stepParameters;
	List<String> processingLog;
	
	StepContext cloneContext() {
		long executionCount0 = executionCount;
		Map<String, Parameter> stepParameters0 = new HashMap<String, Parameter>(stepParameters);
		List<String> processingLog0 = new ArrayList<String>(processingLog);
		
		return new StepContext(executionCount0, stepParameters0, processingLog0);
	}
	
	public long getExecutionCount() {
		return executionCount;
	}
	public long incrementExecutionCount() {
		return ++executionCount;
	}
	
	public Map<String, Parameter> getStepParameters() {
		return Collections.unmodifiableMap(stepParameters);
	}
	public Parameter getStepParameter(String parameterName) {
		return stepParameters.get(parameterName);
	}
	public Parameter removeStepParameter(String parameterName) {
		return stepParameters.remove(parameterName);
	}
	public void putStepParameter(String parameterName, Parameter parameter) {
		Parameter oldPrm = stepParameters.get(parameterName);
		
		if (oldPrm instanceof MappedParameter) {
			try {
				((MappedParameter)oldPrm).update(parameter);
			} catch (WerkParameterException e) {
				throw new RuntimeException(e);
			}
		} else
			stepParameters.put(parameterName, parameter);
	}
	
	public List<String> getProcessingLog() {
		return Collections.unmodifiableList(processingLog);
	}
	public void appendToProcessingLog(String message) {
		processingLog.add(message);
	}
}
