package org.werk.processing.steps;

import java.util.List;
import java.util.Optional;

import lombok.Getter;

public class ExecutionResult<J> {
	@Getter
	protected final StepExecutionStatus status;
	@Getter
	protected final Optional<Long> delayMS;
	@Getter
	protected final Optional<Throwable> exception;
	@Getter
	protected final Optional<List<J>> jobsToJoin;
	@Getter
	protected final Optional<Long> waitForNJobs;
	@Getter
	protected final Optional<String> joinParameterName;
	
	protected ExecutionResult(StepExecutionStatus status, Optional<Long> delayMS, Optional<Throwable> exception, 
			Optional<List<J>> jobsToJoin, Optional<String> joinParameterName, Optional<Long> waitForNJobs) {
		this.status = status; 
		this.delayMS = delayMS;
		this.exception = exception;
		this.jobsToJoin = jobsToJoin;
		this.joinParameterName = joinParameterName;
		this.waitForNJobs = waitForNJobs;
	}

	public static <J> ExecutionResult<J> success() {
		return new ExecutionResult<J>(StepExecutionStatus.SUCCESS,
			Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
	}
	
	public static <J> ExecutionResult<J> failure(Throwable e) {
		return new ExecutionResult<J>(StepExecutionStatus.FAILURE,
			Optional.empty(), Optional.of(e), Optional.empty(), Optional.empty(), Optional.empty());
	}
	
	public static <J> ExecutionResult<J> redo() {
		return new ExecutionResult<J>(StepExecutionStatus.REDO,
			Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
	}
	
	public static <J> ExecutionResult<J> redo(Long delayMS) {
		return new ExecutionResult<J>(StepExecutionStatus.REDO,
			Optional.of(delayMS), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
	}
	
	public static <J> ExecutionResult<J> joinAll(List<J> jobsToJoin, String joinParameterName) {
		return new ExecutionResult<J>(StepExecutionStatus.JOIN, Optional.empty(), Optional.empty(), 
				Optional.of(jobsToJoin), Optional.of(joinParameterName), Optional.empty());
	}
	
	public static <J> ExecutionResult<J> joinN(List<J> jobsToJoin, String joinParameterName, long waitForNJobs) {
		return new ExecutionResult<J>(StepExecutionStatus.JOIN, Optional.empty(), Optional.empty(), 
				Optional.of(jobsToJoin), Optional.of(joinParameterName), Optional.of(waitForNJobs));
	}
}
