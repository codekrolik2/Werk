package org.werk.processing.steps;

import java.util.List;
import java.util.Optional;

import org.werk.processing.steps.callback.WerkCallback;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access=AccessLevel.PRIVATE)
public final class ExecutionResult<J> {
	@Getter
	protected final StepExecutionStatus status;
	@Getter
	protected final Optional<Long> delayMS;
	@Getter
	protected final Optional<Throwable> exception;
	@Getter
	protected final Optional<List<J>> jobsToJoin;
	@Getter
	protected final Optional<String> parameterName;
	@Getter
	protected final Optional<Integer> waitForNJobs;
	@Getter
	protected final Optional<WerkCallback<String>> callback;
	
	public static <J> ExecutionResult<J> success() {
		return new ExecutionResult<J>(StepExecutionStatus.SUCCESS, Optional.empty(), Optional.empty(), 
				Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
	}
	
	public static <J> ExecutionResult<J> failure(Throwable e) {
		return new ExecutionResult<J>(StepExecutionStatus.FAILURE, Optional.empty(), Optional.of(e), 
				Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
	}
	
	public static <J> ExecutionResult<J> redo() {
		return new ExecutionResult<J>(StepExecutionStatus.REDO, Optional.empty(), Optional.empty(), 
				Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
	}
	
	public static <J> ExecutionResult<J> redo(Long delayMS) {
		return new ExecutionResult<J>(StepExecutionStatus.REDO, Optional.of(delayMS), Optional.empty(), 
				Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
	}
	
	public static <J> ExecutionResult<J> joinAll(List<J> jobsToJoin, String joinParameterName) {
		return new ExecutionResult<J>(StepExecutionStatus.JOIN, Optional.empty(), Optional.empty(),
				Optional.of(jobsToJoin), Optional.of(joinParameterName), Optional.of(jobsToJoin.size()), Optional.empty());
	}
	
	public static <J> ExecutionResult<J> joinN(List<J> jobsToJoin, String joinParameterName, int waitForNJobs) {
		return new ExecutionResult<J>(StepExecutionStatus.JOIN, Optional.empty(), Optional.empty(), 
				Optional.of(jobsToJoin), Optional.of(joinParameterName), Optional.of(waitForNJobs), Optional.empty());
	}
	
	public static <J> ExecutionResult<J> waitForCallback(WerkCallback<String> callback, String callbackParameterName) {
		return new ExecutionResult<J>(StepExecutionStatus.CALLBACK, Optional.empty(), Optional.empty(), 
				Optional.empty(), Optional.of(callbackParameterName), Optional.empty(), Optional.of(callback));
	}
	
	public static <J> ExecutionResult<J> waitForCallback(WerkCallback<String> callback, String callbackParameterName, long timeout) {
		return new ExecutionResult<J>(StepExecutionStatus.CALLBACK, Optional.of(timeout), Optional.empty(), 
				Optional.empty(), Optional.of(callbackParameterName), Optional.empty(), Optional.of(callback));
	}
}
