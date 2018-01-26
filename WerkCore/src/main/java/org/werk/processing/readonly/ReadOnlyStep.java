package org.werk.processing.readonly;

import org.werk.data.StepPOJO;
import org.werk.processing.steps.Step;

public interface ReadOnlyStep extends StepPOJO {
	void copyParametersTo(Step step);
}
