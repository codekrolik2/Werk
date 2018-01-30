package org.werk.meta;

import org.werk.processing.steps.Transitioner;

public interface StepTransitionerFactory<J> {
	Transitioner<J> createStepTransitioner() throws Exception;
}
