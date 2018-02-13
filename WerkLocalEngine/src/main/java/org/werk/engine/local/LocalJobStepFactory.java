package org.werk.engine.local;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.pillar.time.interfaces.TimeProvider;
import org.pillar.time.interfaces.Timestamp;
import org.werk.config.WerkConfig;
import org.werk.data.JobPOJO;
import org.werk.data.StepPOJO;
import org.werk.engine.json.JobParameterTool;
import org.werk.engine.processing.WerkStep;
import org.werk.exceptions.WerkConfigException;
import org.werk.meta.JobType;
import org.werk.meta.StepType;
import org.werk.meta.inputparameters.JobInputParameter;
import org.werk.processing.jobs.Job;
import org.werk.processing.jobs.JobStatus;
import org.werk.processing.jobs.JoinStatusRecord;
import org.werk.processing.parameters.Parameter;
import org.werk.processing.steps.StepExec;
import org.werk.processing.steps.StepProcessingLogRecord;
import org.werk.processing.steps.Transitioner;
import org.werk.util.JoinResultSerializer;

public abstract class LocalJobStepFactory<J> {
	protected WerkConfig<J> werkConfig;
	protected TimeProvider timeProvider;
	protected LocalJobManager<J> localJobManager;
	protected JoinResultSerializer<J> joinResultSerializer;
	protected JobParameterTool jobParameterTool = new JobParameterTool();
	
	public LocalJobStepFactory(WerkConfig<J> werkConfig, TimeProvider timeProvider, LocalJobManager<J> localJobManager,
			JoinResultSerializer<J> joinResultSerializer) {
		this.werkConfig = werkConfig;
		this.timeProvider = timeProvider;
		this.localJobManager = localJobManager;
		this.joinResultSerializer = joinResultSerializer;
	}
	
	protected abstract J getNextJobId();
	
	public LocalWerkJob<J> createNewJob(String jobTypeName, Map<String, Parameter> jobInitialParameters, 
			Optional<String> jobName, Optional<J> parentJob) throws Exception {
		JobType jobType = werkConfig.getJobTypeLatestVersion(jobTypeName);
		if (jobType == null)
			throw new WerkConfigException(
				String.format("JobType not found [%s]", jobTypeName)
			);
		
		//Check initial parameters
		List<JobInputParameter> parameterSet = jobParameterTool.findMatchingParameterSet(jobType, jobInitialParameters);
		
		//Check enum and range parameters
		jobParameterTool.checkRangeAndEnumParameters(parameterSet, jobInitialParameters);
		
		//Fill default parameters
		jobParameterTool.fillParameters(parameterSet, jobInitialParameters);
		
		long version = jobType.getVersion();
		JobStatus status = JobStatus.UNDEFINED;
		Map<String, Parameter> jobParameters = new HashMap<>(jobInitialParameters);
		Timestamp nextExecutionTime = timeProvider.getCurrentTime();
		Optional<JoinStatusRecord<J>> joinStatusRecord = Optional.empty();
		
		return new LocalWerkJob<J>(getNextJobId(), jobType, version, jobName, status, 
				jobInitialParameters, jobParameters, 0, nextExecutionTime, nextExecutionTime, joinStatusRecord, parentJob,
				localJobManager, joinResultSerializer);
	}

	public LocalWerkJob<J> createJobOfVersion(String jobTypeName, long oldVersion, Map<String, Parameter> jobInitialParameters, 
			Optional<String> jobName, Optional<J> parentJob) throws Exception {
		JobType jobType = werkConfig.getJobTypeForAnyVersion(oldVersion, jobTypeName);
		if (jobType == null)
			throw new WerkConfigException(
				String.format("JobType not found [%s] for version [%d]", jobTypeName, oldVersion)
			);
		
		//Check initial parameters
		List<JobInputParameter> parameterSet = jobParameterTool.findMatchingParameterSet(jobType, jobInitialParameters);

		//Check enum and range parameters
		jobParameterTool.checkRangeAndEnumParameters(parameterSet, jobInitialParameters);
		
		//Fill default parameters
		jobParameterTool.fillParameters(parameterSet, jobInitialParameters);
		
		long version = jobType.getVersion();
		JobStatus status = JobStatus.UNDEFINED;
		Map<String, Parameter> jobParameters = new HashMap<>(jobInitialParameters);
		Timestamp nextExecutionTime = timeProvider.getCurrentTime();
		Optional<JoinStatusRecord<J>> joinStatusRecord = Optional.empty();
		
		return new LocalWerkJob<J>(getNextJobId(), jobType, version, jobName, status, 
				jobInitialParameters, jobParameters, 0, nextExecutionTime, nextExecutionTime, joinStatusRecord, parentJob,
				localJobManager, joinResultSerializer);
	}

	public LocalWerkJob<J> createJob(JobPOJO<J> job) throws Exception {
		String jobTypeName = job.getJobTypeName();
		long version = job.getVersion();
		
		JobType jobType = werkConfig.getJobTypeForAnyVersion(version, jobTypeName);
		if (jobType == null)
			throw new WerkConfigException(
				String.format("JobType not found [%s]", jobTypeName)
			);
		
		Optional<String> jobName = job.getJobName();
		JobStatus status = job.getStatus();
		Map<String, Parameter> jobInitialParameters = job.getJobInitialParameters();
		Map<String, Parameter> jobParameters = job.getJobParameters();
		Timestamp creationtime = job.getCreationTime();
		Timestamp nextExecutionTime = job.getNextExecutionTime();
		Optional<JoinStatusRecord<J>> joinStatusRecord = job.getJoinStatusRecord();
		Optional<J> parentJob = job.getParentJobId();
		int stepCount = job.getStepCount();
		
		return new LocalWerkJob<J>(((LocalWerkJob<J>)job).getJobId(), jobType, version, jobName, status, 
				jobInitialParameters, jobParameters, stepCount, creationtime, nextExecutionTime, joinStatusRecord, parentJob,
				localJobManager, joinResultSerializer);
	}

	//---------------------------------------------
	
	public WerkStep<J> createStep(Job<J> job, StepPOJO step) throws Exception {
		String stepTypeName = step.getStepTypeName();
		StepType<J> stepType = werkConfig.getStepType(stepTypeName);
		
		int stepNumber = step.getStepNumber();
		List<Integer> rollbackStepNumber = step.getRollbackStepNumbers();
		int executionCount = step.getExecutionCount(); 
		Map<String, Parameter> stepParameters = step.getStepParameters();
		List<StepProcessingLogRecord> processingLog = step.getProcessingLog();

		StepExec<J> stepExec = werkConfig.getStepExec(stepTypeName);
		Transitioner<J> stepTransitioner = werkConfig.getStepTransitioner(stepTypeName); 
		
		return new WerkStep<J>(job, stepType, (job.getStatus() == JobStatus.ROLLING_BACK), stepNumber, rollbackStepNumber, 
				executionCount, stepParameters, processingLog, stepExec, stepTransitioner, timeProvider);
	}

	public WerkStep<J> createFirstStep(Job<J> job, int stepNumber) throws Exception {
		JobType jobType = werkConfig.getJobTypeForAnyVersion(job.getVersion(), job.getJobTypeName());
		if (jobType == null)
			throw new WerkConfigException(
				String.format("JobType not found [%s] for version [%d]", job.getJobTypeName(), job.getVersion())
			);
			
		return createNewStep(job, stepNumber, jobType.getFirstStepTypeName());
	}

	public WerkStep<J> createNewStep(Job<J> job, int stepNumber, String stepType) throws Exception {
		return createNewStep(job, stepNumber, Optional.empty(), stepType);
	}

	public WerkStep<J> createNewStep(Job<J> job, int stepNumber, Optional<List<Integer>> rollbackStepNumbers, 
			String stepType) throws Exception {
		String stepTypeName = stepType;
		StepType<J> stepTypeObj = werkConfig.getStepType(stepTypeName);
		
		int executionCount = 0; 
		Map<String, Parameter> stepParameters = new HashMap<>();
		List<StepProcessingLogRecord> processingLog = new ArrayList<>();

		StepExec<J> stepExec = werkConfig.getStepExec(stepType);
		Transitioner<J> stepTransitioner = werkConfig.getStepTransitioner(stepType); 
		
		List<Integer> rollbackStepNumbersLst;
		if (rollbackStepNumbers.isPresent())
			rollbackStepNumbersLst = rollbackStepNumbers.get();
		else
			rollbackStepNumbersLst = new ArrayList<>();
		
		return new WerkStep<J>(job, stepTypeObj, (job.getStatus() == JobStatus.ROLLING_BACK), stepNumber, rollbackStepNumbersLst,
				executionCount, stepParameters, processingLog, stepExec, stepTransitioner, timeProvider); 
	}
	
	//-------------------------------------------------------
	
}
