package org.werk.engine.processing;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.werk.processing.jobs.Job;
import org.werk.processing.parameters.Parameter;
import org.werk.processing.steps.ExecutionResult;
import org.werk.processing.steps.Step;
import org.werk.processing.steps.StepExec;
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
	@Getter
	protected long stepNumber;
	@Getter
	protected List<Long> rollbackStepNumbers;
	
	//------------------------------------------------
	
	@Getter
	protected StepContext mainContext;
	
	@Getter
	protected StepContext tempContext;
	
	public WerkStep(Job job, String stepTypeName, long stepNumber, List<Long> rollbackStepNumbers, 
			long executionCount, Map<String, Parameter> stepParameters, List<String> processingLog, 
			StepExec stepExec, StepTransitioner stepTransitioner) {
		this.job = job;
		this.stepTypeName = stepTypeName;
		this.stepNumber = stepNumber;
		this.rollbackStepNumbers = rollbackStepNumbers;
		
		mainContext = new StepContext(executionCount, stepParameters, processingLog);
		
		this.stepExec = stepExec;
		this.stepTransitioner = stepTransitioner;
	}
	
	public void openTempContext() {
		if (tempContext != null)
			throw new IllegalStateException("Temp context already opened");
		
		tempContext = mainContext.cloneContext();
	}
	
	public void commitTempContext() {
		mainContext = tempContext;
		tempContext = null;
	}
	
	public void rollbackTempContext() {
		if (tempContext == null)
			throw new IllegalStateException("Temp context not opened");
		
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
	
	@Override
	public Long getLongParameter(String parameterName) {
		return getCurrentContext().getLongParameter(parameterName);
	}

	@Override
	public void putLongParameter(String parameterName, Long value) {
		getCurrentContext().putLongParameter(parameterName, value);
	}
	
	@Override
	public Double getDoubleParameter(String parameterName) {
		return getCurrentContext().getDoubleParameter(parameterName);
	}

	@Override
	public void putDoubleParameter(String parameterName, Double value) {
		getCurrentContext().putDoubleParameter(parameterName, value);
	}
	
	@Override
	public Boolean getBoolParameter(String parameterName) {
		return getCurrentContext().getBoolParameter(parameterName);
	}

	@Override
	public void putBoolParameter(String parameterName, Boolean value) {
		getCurrentContext().putBoolParameter(parameterName, value);
	}
	
	@Override
	public String getStringParameter(String parameterName) {
		return getCurrentContext().getStringParameter(parameterName);
	}

	@Override
	public void putStringParameter(String parameterName, String value) {
		getCurrentContext().putStringParameter(parameterName, value);
	}
	
	@Override
	public Map<String, Parameter> getDictionaryParameter(String parameterName) {
		return getCurrentContext().getDictionaryParameter(parameterName);
	}

	@Override
	public void putDictionaryParameter(String parameterName, Map<String, Parameter> value) {
		getCurrentContext().putDictionaryParameter(parameterName, value);
	}
	
	@Override
	public List<Parameter> getListParameter(String parameterName) {
		return getCurrentContext().getListParameter(parameterName);
	}

	@Override
	public void putListParameter(String parameterName, List<Parameter> value) {
		getCurrentContext().putListParameter(parameterName, value);
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

	protected String stepExecutionResultToStr(ExecutionResult record) {
		if (record.getStatus() == StepExecutionStatus.FAILURE) {
			return String.format("ExecutionStatus: %s; Exception [%s]", record.getStatus().toString(), record.getException().get().toString());
		} else if (record.getStatus() == StepExecutionStatus.REDO) {
			if (record.getDelayMS().isPresent())
				return String.format("ExecutionStatus: %s; Delay [%d]", record.getStatus().toString(), record.getDelayMS().get());
			else
				return String.format("ExecutionStatus: %s; No delay", record.getStatus().toString());
		} else if (record.getStatus() == StepExecutionStatus.JOIN) {
			return String.format("ExecutionStatus: %s; JobList [%s]", record.getStatus().toString(), record.getJobsToJoin().get().toString());
		} else {// if (record.getStatus() == StepExecutionStatus.SUCCESS) {
			return String.format("ExecutionStatus: %s", record.getStatus().toString());
		}
	}
	
	@Override
	public ExecutionResult appendToProcessingLog(ExecutionResult record) {
		appendToProcessingLog(stepExecutionResultToStr(record));
		return record;
	}

	@Override
	public ExecutionResult appendToProcessingLog(ExecutionResult record, String message) {
		appendToProcessingLog(String.format("%s [%s]", stepExecutionResultToStr(record), message));
		return record;
	}

	protected String transitionToStr(Transition record) {
		if (record.getTransitionStatus() == TransitionStatus.NEXT_STEP) {
			if (record.getDelayMS().isPresent()) {
				return String.format("Transition: %s; Next step name [%s]; Delay [%d]", 
						record.getTransitionStatus().toString(), record.getStepName().get(), record.getDelayMS().get());
			} else {
				return String.format("Transition: %s; Next step name [%s]; No delay", 
						record.getTransitionStatus().toString(), record.getStepName().get());
			}
		} else if (record.getTransitionStatus() == TransitionStatus.ROLLBACK) {
			if (record.getStepName().isPresent()) {
				if (record.getDelayMS().isPresent()) {
					return String.format("Transition: %s; Rollback step name [%s]; Delay [%d]", 
						record.getTransitionStatus().toString(), record.getStepName().get(), record.getDelayMS().get());
				} else {
					return String.format("Transition: %s; Rollback step name [%s]; No delay", 
						record.getTransitionStatus().toString(), record.getStepName().get());
				}
			} else {
				if (record.getDelayMS().isPresent()) {
					return String.format("Transition: %s; Rollback step numbers [%d]; Delay [%d]", 
						record.getTransitionStatus().toString(), record.getRollbackStepNumbers(), record.getDelayMS().get());
				} else {
					return String.format("Transition: %s; Rollback step numbers [%d]; No delay", 
						record.getTransitionStatus().toString(), record.getRollbackStepNumbers());
				}
			}
		} else { //if ((record.getTransitionStatus() == TransitionStatus.FINISH) || (record.getTransitionStatus() == TransitionStatus.FAIL)) {
			return String.format("Transition: %s", record.getTransitionStatus().toString());
		} 
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
