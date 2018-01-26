package org.werk.processing.steps;

import org.werk.processing.jobs.Job;

public interface StepTransitioner {
	Transition transition(boolean isSuccess, Job job);
}
