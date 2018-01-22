package org.werk.meta;

import org.werk.steps.StepTransitioner;

public interface StepTransitionerFactory {
	StepTransitioner createStepTransitioner() throws Exception;
}
