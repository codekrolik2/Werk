package org.werk.processing.steps;

public interface StepTransitioner {
	Transition transition(StepExecutionResult result, Step step);
}
