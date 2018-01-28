package org.werk.engine;

import org.werk.processing.jobs.Job;
import org.werk.processing.steps.ExecutionResult;
import org.werk.processing.steps.Transition;

public interface WerkStepSwitcher<J> {
	StepSwitchResult redo(Job<J> job, ExecutionResult<J> exec);
	StepSwitchResult join(Job<J> job, ExecutionResult<J> exec);
	StepSwitchResult stepExecError(Job<J> job, Exception e);
	
	StepSwitchResult transition(Job<J> job, Transition transition);
	StepSwitchResult stepTransitionError(Job<J> job, Exception e);
}
