package org.werk.engine;

import org.werk.data.JobPOJO;
import org.werk.data.StepPOJO;
import org.werk.processing.jobs.Job;
import org.werk.processing.steps.Step;

public interface JobStepFactory {
	public Job createJob(JobPOJO job);
	public Step createStep(StepPOJO step);
	
	public Step createNewStep(String jobType, String stepType);
}
