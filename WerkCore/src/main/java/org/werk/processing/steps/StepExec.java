package org.werk.processing.steps;

public interface StepExec {
	StepExecutionResult process(Step step);
	StepExecutionResult rollback(Step step);
}
