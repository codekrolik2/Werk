package org.werk.processing.steps;

public interface Transitioner<J> {
	Transition processingTransition(boolean isSuccess, Step<J> step);
	Transition rollbackTransition(boolean isSuccess, Step<J> step);
}
