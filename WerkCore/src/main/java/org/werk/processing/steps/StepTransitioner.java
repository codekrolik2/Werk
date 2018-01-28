package org.werk.processing.steps;

import org.werk.processing.jobs.Job;

public interface StepTransitioner<J> {
	Transition transition(boolean isSuccess, Job<J> job);
}
