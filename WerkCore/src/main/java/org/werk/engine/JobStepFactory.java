package org.werk.engine;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.werk.data.JobPOJO;
import org.werk.data.StepPOJO;
import org.werk.processing.jobs.Job;
import org.werk.processing.parameters.Parameter;
import org.werk.processing.steps.Step;

public interface JobStepFactory<J> {
	public Job<J> createNewJob(String jobTypeName, Map<String, Parameter> jobInitialParameters,
			Optional<String> jobName, Optional<J> parentJob) throws Exception;
	public Job<J> createOldVersionJob(String jobTypeName, long oldVersion, Map<String, Parameter> jobInitialParameters,
			Optional<String> jobName, Optional<J> parentJob) throws Exception;
	
	public Step<J> createFirstStep(Job<J> job, int stepNumber) throws Exception;
	
	public Step<J> createNewStep(Job<J> job, int stepNumber, String stepType) throws Exception;
	public Step<J> createNewStep(Job<J> job, int stepNumber, Optional<List<Integer>> rollbackStepNumbers, String stepType) throws Exception;
	
	public Job<J> createJob(JobPOJO<J> job) throws Exception;
	public Step<J> createStep(Job<J> job, StepPOJO step) throws Exception;
}
