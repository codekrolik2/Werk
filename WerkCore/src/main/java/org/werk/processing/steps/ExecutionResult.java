package org.werk.processing.steps;

import java.util.List;
import java.util.Optional;

import org.werk.processing.jobs.JobToken;

import lombok.Getter;

public class ExecutionResult {
	@Getter
	protected final StepExecutionStatus status;
	@Getter
	protected final Optional<Long> delayMS;
	@Getter
	protected final Optional<Throwable> exception;
	@Getter
	protected final Optional<List<JobToken>> jobsToJoin;
	
	protected ExecutionResult(StepExecutionStatus status, Optional<Long> delayMS,
			Optional<Throwable> exception, Optional<List<JobToken>> jobsToJoin) {
		this.status = status; 
		this.delayMS = delayMS;
		this.exception = exception;
		this.jobsToJoin = jobsToJoin;
	}

	public static ExecutionResult success() {
		return new ExecutionResult(StepExecutionStatus.SUCCESS,
			Optional.empty(), Optional.empty(), Optional.empty());
	}
	
	public static ExecutionResult failure(Throwable e) {
		return new ExecutionResult(StepExecutionStatus.FAILURE,
			Optional.empty(), Optional.of(e), Optional.empty());
	}
	
	public static ExecutionResult redo() {
		return new ExecutionResult(StepExecutionStatus.REDO,
			Optional.empty(), Optional.empty(), Optional.empty());
	}
	
	public static ExecutionResult redo(Long delayMS) {
		return new ExecutionResult(StepExecutionStatus.REDO,
			Optional.of(delayMS), Optional.empty(), Optional.empty());
	}
	
	public static ExecutionResult join(List<JobToken> jobsToJoin) {
		return new ExecutionResult(StepExecutionStatus.JOIN,
			Optional.empty(), Optional.empty(), Optional.of(jobsToJoin));
	}
}
