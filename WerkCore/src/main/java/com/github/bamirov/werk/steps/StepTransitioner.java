package com.github.bamirov.werk.steps;

public interface StepTransitioner {
	void transitionJob(StepExecutionResult result, Step step);
}
