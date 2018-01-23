package org.werk.engine.processing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.werk.jobs.Job;
import org.werk.parameters.interfaces.Parameter;
import org.werk.steps.Step;
import org.werk.steps.StepExec;
import org.werk.steps.StepExecutionResult;
import org.werk.steps.StepExecutionStatus;
import org.werk.steps.StepTransitioner;
import org.werk.steps.TransitionResult;
import org.werk.steps.TransitionStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class WerkStep implements Step {
	@Getter
	protected Job job;
	@Getter
	protected String stepTypeName;
	@Getter
	protected StepExec stepExec;
	@Getter
	protected StepTransitioner stepTransitioner;
	
	//------------------------------------------------
	
	
	protected StepContext mainContext;
	protected StepContext tempContext;
	
	public StepContext setTempContext(StepContext tempContext) {
		this.tempContext = tempContext;
		return tempContext;
	}
	
	protected StepContext getCurrentContext() {
		return tempContext == null ? mainContext : tempContext;
	}
	
	//------------------------------------------------
	
	@Override
	public long getExecutionCount() {
		return getCurrentContext().executionCount;
	}

	public long incrementExecutionCount() {
		getCurrentContext().executionCount++;
		return getCurrentContext().executionCount;
	}
	
	//------------------------------------------------
	
	@Override
	public Map<String, Parameter> getStepParameters() {
		return Collections.unmodifiableMap(getCurrentContext().stepParameters);
	}

	@Override
	public Parameter getStepParameter(String parameterName) {
		return getCurrentContext().stepParameters.get(parameterName);
	}

	@Override
	public Parameter removeStepParameter(String parameterName) {
		return getCurrentContext().stepParameters.remove(parameterName);
	}

	@Override
	public void putStepParameter(String parameterName, Parameter parameter) {
		getCurrentContext().stepParameters.put(parameterName, parameter);
	}

	//------------------------------------------------
	
	@Override
	public List<String> getProcessingLog() {
		return Collections.unmodifiableList(getCurrentContext().processingLog);
	}

	@Override
	public void appendToProcessingLog(String message) {
		getCurrentContext().processingLog.add(message);
	}

	protected String stepExecutionResultToStr(StepExecutionResult record) {
		return (record.getStatus() == StepExecutionStatus.REDO) 
			?
				String.format("ExecutionStatus: %s; restart in: %s", 
					record.getStatus().toString(), record.getDelayMS().get())
			:
				String.format("ExecutionStatus: %s", record.getStatus().toString());
	}
	
	@Override
	public StepExecutionResult appendToProcessingLog(StepExecutionResult record) {
		appendToProcessingLog(stepExecutionResultToStr(record));
		return record;
	}

	@Override
	public StepExecutionResult appendToProcessingLog(StepExecutionResult record, String message) {
		appendToProcessingLog(String.format("%s [%s]", stepExecutionResultToStr(record), message));
		return record;
	}

	protected String transitionResultToStr(TransitionResult record) {
		return ((record.getTransitionStatus() == TransitionStatus.NEXT_STEP) ||
				(record.getTransitionStatus() == TransitionStatus.ROLLBACK))
			?
				String.format("Transition: %s; next step: %s", 
					record.getTransitionStatus().toString(), record.stepName().get())
			:
				String.format("Transition: %s", record.getTransitionStatus().toString());
	}
	
	@Override
	public TransitionResult appendToProcessingLog(TransitionResult transitionResult) {
		appendToProcessingLog(transitionResultToStr(transitionResult));
		return transitionResult;
	}

	@Override
	public TransitionResult appendToProcessingLog(TransitionResult transitionResult, String message) {
		appendToProcessingLog(String.format("%s [%s]", transitionResultToStr(transitionResult), message));
		return transitionResult;
	}
}
