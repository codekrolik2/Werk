package org.werk.engine.processing;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.werk.processing.jobs.Job;
import org.werk.processing.parameters.Parameter;
import org.werk.processing.steps.Step;
import org.werk.processing.steps.StepExec;
import org.werk.processing.steps.StepExecutionResult;
import org.werk.processing.steps.StepExecutionStatus;
import org.werk.processing.steps.StepTransitioner;
import org.werk.processing.steps.Transition;
import org.werk.processing.steps.TransitionStatus;

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
	
	@Getter
	protected StepContext mainContext;
	
	@Getter
	protected StepContext tempContext;
	
	public void openTempContext() {
		if (tempContext != null)
			throw new IllegalStateException("Temp context already opened");
		
		tempContext = mainContext.cloneContext();
	}
	
	public void commitTempContext() {
		mainContext = tempContext;
		tempContext = null;
	}
	
	protected StepContext getCurrentContext() {
		return tempContext == null ? mainContext : tempContext;
	}
	
	//------------------------------------------------
	
	@Override
	public long getExecutionCount() {
		return getCurrentContext().executionCount;
	}

	@Override
	public long incrementExecutionCount() {
		getCurrentContext().executionCount++;
		return getCurrentContext().executionCount;
	}
	
	//------------------------------------------------
	
	@Override
	public Map<String, Parameter> getStepParameters() {
		return getCurrentContext().getParameters();
	}

	@Override
	public Parameter getStepParameter(String parameterName) {
		return getCurrentContext().getParameter(parameterName);
	}

	@Override
	public Parameter removeStepParameter(String parameterName) {
		return getCurrentContext().removeParameter(parameterName);
	}

	@Override
	public void putStepParameter(String parameterName, Parameter parameter) {
		getCurrentContext().putParameter(parameterName, parameter);
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

	protected String transitionToStr(Transition record) {
		return ((record.getTransitionStatus() == TransitionStatus.NEXT_STEP) ||
				(record.getTransitionStatus() == TransitionStatus.ROLLBACK))
			?
				String.format("Transition: %s; next step: %s", 
					record.getTransitionStatus().toString(), record.stepName().get())
			:
				String.format("Transition: %s", record.getTransitionStatus().toString());
	}
	
	@Override
	public Transition appendToProcessingLog(Transition transition) {
		appendToProcessingLog(transitionToStr(transition));
		return transition;
	}

	@Override
	public Transition appendToProcessingLog(Transition transition, String message) {
		appendToProcessingLog(String.format("%s [%s]", transitionToStr(transition), message));
		return transition;
	}
}
