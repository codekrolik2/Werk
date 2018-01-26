package org.werk.processing.steps;

public interface StepExec {
	ExecutionResult process(Step step);
	ExecutionResult rollback(Step step);
}
