package org.werk.processing.steps;

public interface StepExec<J> {
	ExecutionResult<J> process(Step<J> step);
	ExecutionResult<J> rollback(Step<J> step);
}
