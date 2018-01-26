package org.werk.engine;

import org.werk.processing.jobs.Job;
import org.werk.processing.steps.ExecutionResult;
import org.werk.processing.steps.Transition;

public interface WerkStepSwitcher {
	StepSwitchResult redo(Job job, ExecutionResult exec);
	StepSwitchResult join(Job job, ExecutionResult exec);
	StepSwitchResult stepExecError(Job job, Exception e);
	
	StepSwitchResult transition(Job job, Transition transition);
	StepSwitchResult stepTransitionError(Job job, Exception e);
}
