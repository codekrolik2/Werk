package org.werk.engine;

import org.werk.processing.jobs.Job;
import org.werk.processing.steps.Transition;

public interface WerkStepSwitcher {
	StepSwitchResult switchStep(Job job, Transition transition) throws Exception;
	StepSwitchResult stepExecError(Job job, Exception e) throws Exception;
	StepSwitchResult stepTransitionError(Job job, Exception e) throws Exception;
}
