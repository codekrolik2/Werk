package org.werk.meta;

import org.werk.processing.steps.StepTransitioner;

public interface StepTransitionerFactory<J> {
	StepTransitioner<J> createStepTransitioner() throws Exception;
}
