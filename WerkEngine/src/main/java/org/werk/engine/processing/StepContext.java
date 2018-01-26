package org.werk.engine.processing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.werk.processing.parameters.Parameter;

public class StepContext extends ParameterContext {
	protected long executionCount;
	protected List<String> processingLog;

	public StepContext cloneContext() {
		long executionCount0 = executionCount;
		
		Map<String, Parameter> stepParameters0 = new HashMap<>();
		for (Map.Entry<String, Parameter> stepParameter : parameters.entrySet())
			stepParameters0.put(stepParameter.getKey(), cloneParameter(stepParameter.getValue()));
		List<String> processingLog0 = new ArrayList<>(processingLog);
		
		return new StepContext(executionCount0, stepParameters0, processingLog0);
	}
	
	public StepContext(long executionCount, Map<String, Parameter> stepParameters, List<String> processingLog) {
		super(stepParameters);
		this.executionCount = executionCount;
		this.processingLog = processingLog;
	}
	
	public long getExecutionCount() {
		return executionCount;
	}
	
	public long incrementExecutionCount() {
		return ++executionCount;
	}
	
	public List<String> getProcessingLog() {
		return Collections.unmodifiableList(processingLog);
	}
	
	public void appendToProcessingLog(String message) {
		processingLog.add(message);
	}
}
