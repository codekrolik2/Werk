package com.github.bamirov.werk.steps;

public interface StepExec {
	StepExecutionResult process(Step step);
	StepExecutionResult rollback(Step step);
}
