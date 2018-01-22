package org.werk.steps;

public interface StepTransitioner {
	void transitionJob(StepExecutionResult result, Step step);
}
