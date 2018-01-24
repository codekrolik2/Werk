package org.werk.meta;

import org.werk.processing.steps.StepTransitioner;

public interface StepTransitionerFactory {
	StepTransitioner createStepTransitioner() throws Exception;
}
