package org.werk.engine;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.werk.data.JobPOJO;
import org.werk.data.StepPOJO;
import org.werk.processing.jobs.Job;
import org.werk.processing.jobs.JobToken;
import org.werk.processing.parameters.Parameter;
import org.werk.processing.steps.Step;

public interface JobStepFactory {
	public Job createNewJob(String jobTypeName, Map<String, Parameter> jobInitialParameters,
			Optional<String> jobName, Optional<JobToken> parentJob) throws Exception;
	public Job createOldVersionJob(String jobTypeName, long oldVersion, Map<String, Parameter> jobInitialParameters,
			Optional<String> jobName, Optional<JobToken> parentJob) throws Exception;
	
	public Step createFirstStep(Job job, long stepNumber) throws Exception;
	
	public Step createNewStep(Job job, long stepNumber, String stepType) throws Exception;
	public Step createNewStep(Job job, long stepNumber, List<Long> rollbackStepNumbers, String stepType) throws Exception;
	
	public Job createJob(JobPOJO job) throws Exception;
	public Step createStep(Job job, StepPOJO step) throws Exception;
}
