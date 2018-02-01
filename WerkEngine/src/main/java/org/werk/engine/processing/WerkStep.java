package org.werk.engine.processing;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.pillar.time.interfaces.TimeProvider;
import org.werk.data.StepPOJO;
import org.werk.exceptions.StepLogLimitExceededException;
import org.werk.meta.StepType;
import org.werk.processing.jobs.Job;
import org.werk.processing.parameters.Parameter;
import org.werk.processing.steps.ExecutionResult;
import org.werk.processing.steps.Step;
import org.werk.processing.steps.StepExec;
import org.werk.processing.steps.StepExecutionStatus;
import org.werk.processing.steps.StepProcessingLogRecord;
import org.werk.processing.steps.Transition;
import org.werk.processing.steps.TransitionStatus;
import org.werk.processing.steps.Transitioner;

import lombok.Getter;

public class WerkStep<J> implements Step<J> {
	@Getter
	protected Job<J> job;
	@Getter
	protected StepType<J> stepType;
	@Getter
	protected StepExec<J> stepExec;
	@Getter
	protected Transitioner<J> stepTransitioner;
	@Getter
	protected long stepNumber;
	@Getter
	protected List<Long> rollbackStepNumbers;
	@Getter
	protected boolean isRollback;
	
	//------------------------------------------------
	
	@Getter
	protected StepContext<J> mainContext;
	
	@Getter
	protected StepContext<J> tempContext;
	
	protected TimeProvider timeProvider;
	
	public WerkStep(Job<J> job, StepType<J> stepType, boolean isRollback, long stepNumber, List<Long> rollbackStepNumbers, 
			long executionCount, Map<String, Parameter> stepParameters, List<StepProcessingLogRecord> processingLog, 
			StepExec<J> stepExec, Transitioner<J> stepTransitioner, TimeProvider timeProvider) {
		this.job = job;
		this.stepType = stepType;
		this.isRollback = isRollback;
		this.stepNumber = stepNumber;
		this.rollbackStepNumbers = rollbackStepNumbers;
		
		mainContext = new StepContext<J>(this.getStepType(), timeProvider, executionCount, stepParameters, processingLog);
		
		this.stepExec = stepExec;
		this.stepTransitioner = stepTransitioner;
		
		this.timeProvider = timeProvider;
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
	
	public StepContext<J> getCurrentContext() {
		return tempContext == null ? mainContext : tempContext;
	}
	
	//------------------------------------------------

	@Override
	public String getStepTypeName() {
		return stepType.getStepTypeName();
	}
	
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
	public List<StepProcessingLogRecord> getProcessingLog() {
		return Collections.unmodifiableList(getCurrentContext().processingLog);
	}

	@Override
	public void appendToProcessingLog(String message) throws StepLogLimitExceededException {
		getCurrentContext().appendToProcessingLog(message);
	}

	protected String stepExecutionResultToStr(ExecutionResult<J> record) {
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
	public ExecutionResult<J> appendToProcessingLog(ExecutionResult<J> record) throws StepLogLimitExceededException {
		appendToProcessingLog(stepExecutionResultToStr(record));
		return record;
	}

	@Override
	public ExecutionResult<J> appendToProcessingLog(ExecutionResult<J> record, String message) throws StepLogLimitExceededException {
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
	public Transition appendToProcessingLog(Transition transition) throws StepLogLimitExceededException {
		appendToProcessingLog(transitionToStr(transition));
		return transition;
	}

	@Override
	public Transition appendToProcessingLog(Transition transition, String message) throws StepLogLimitExceededException {
		appendToProcessingLog(String.format("%s [%s]", transitionToStr(transition), message));
		return transition;
	}

	@Override
	public void copyParametersFrom(StepPOJO step) {
		for (Entry<String, Parameter> ent : step.getStepParameters().entrySet()) {
			String parameterKey = ent.getKey();
			Parameter parameterValue = ent.getValue();
			
			putStepParameter(parameterKey, parameterValue);
		}
	}
}
