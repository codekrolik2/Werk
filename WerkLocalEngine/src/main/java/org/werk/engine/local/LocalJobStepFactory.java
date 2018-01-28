package org.werk.engine.local;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.pillar.time.interfaces.TimeProvider;
import org.pillar.time.interfaces.Timestamp;
import org.werk.config.WerkConfig;
import org.werk.data.JobPOJO;
import org.werk.data.StepPOJO;
import org.werk.engine.JobStepFactory;
import org.werk.engine.processing.WerkStep;
import org.werk.exceptions.WerkConfigException;
import org.werk.meta.JobType;
import org.werk.meta.StepType;
import org.werk.meta.inputparameters.DefaultValueJobInputParameter;
import org.werk.meta.inputparameters.JobInputParameter;
import org.werk.processing.jobs.Job;
import org.werk.processing.jobs.JobStatus;
import org.werk.processing.jobs.JobToken;
import org.werk.processing.jobs.JoinStatusRecord;
import org.werk.processing.parameters.BoolParameter;
import org.werk.processing.parameters.DictionaryParameter;
import org.werk.processing.parameters.DoubleParameter;
import org.werk.processing.parameters.ListParameter;
import org.werk.processing.parameters.LongParameter;
import org.werk.processing.parameters.Parameter;
import org.werk.processing.parameters.StringParameter;
import org.werk.processing.steps.Step;
import org.werk.processing.steps.StepExec;
import org.werk.processing.steps.StepTransitioner;

public class LocalJobStepFactory implements JobStepFactory {
	protected AtomicLong jobIdCounter;
	protected WerkConfig werkConfig;
	protected TimeProvider timeProvider;
	
	public LocalJobStepFactory(WerkConfig werkConfig, TimeProvider timeProvider) {
		jobIdCounter = new AtomicLong(0);
		this.werkConfig = werkConfig;
		this.timeProvider = timeProvider;
	}
	
	@Override
	public Job createNewJob(String jobTypeName, Map<String, Parameter> jobInitialParameters, 
			Optional<String> jobName, Optional<JobToken> parentJob) throws Exception {
		JobType jobType = werkConfig.getJobTypeLatestVersion(jobTypeName);
		if (jobType == null)
			throw new WerkConfigException(
				String.format("JobType not found [%s]", jobTypeName)
			);
		
		checkParameters(jobType, jobInitialParameters);
		
		long version = jobType.getVersion();
		JobStatus status = JobStatus.INACTIVE;
		Map<String, Parameter> jobParameters = new HashMap<>();
		Timestamp nextExecutionTime = timeProvider.getCurrentTime();
		Optional<JoinStatusRecord> joinStatusRecord = Optional.empty();

		return new LocalWerkJob(jobIdCounter.incrementAndGet(), jobTypeName, version, jobName, status, 
				jobInitialParameters, jobParameters, nextExecutionTime, joinStatusRecord, parentJob);
	}

	@Override
	public Job createOldVersionJob(String jobTypeName, long oldVersion, Map<String, Parameter> jobInitialParameters, 
			Optional<String> jobName, Optional<JobToken> parentJob) throws Exception {
		JobType jobType = werkConfig.getJobTypeForOldVersion(oldVersion, jobTypeName);
		if (jobType == null)
			throw new WerkConfigException(
				String.format("JobType not found [%s] for version [%d]", jobTypeName, oldVersion)
			);
		
		checkParameters(jobType, jobInitialParameters);
		
		long version = jobType.getVersion();
		JobStatus status = JobStatus.INACTIVE;
		Map<String, Parameter> jobParameters = new HashMap<>();
		Timestamp nextExecutionTime = timeProvider.getCurrentTime();
		Optional<JoinStatusRecord> joinStatusRecord = Optional.empty();

		return new LocalWerkJob(jobIdCounter.incrementAndGet(), jobTypeName, version, jobName, status, 
				jobInitialParameters, jobParameters, nextExecutionTime, joinStatusRecord, parentJob);
	}

	@Override
	public Job createJob(JobPOJO job) throws Exception {
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
		Timestamp nextExecutionTime = job.getNextExecutionTime();
		Optional<JoinStatusRecord> joinStatusRecord = job.getJoinStatusRecord();
		Optional<JobToken> parentJob = job.getParentJobToken();

		return new LocalWerkJob(((LocalWerkJob)job).getJobId(), jobTypeName, version, jobName, status, 
				jobInitialParameters, jobParameters, nextExecutionTime, joinStatusRecord, parentJob);
	}

	//---------------------------------------------
	
	@Override
	public Step createStep(Job job, StepPOJO step) throws Exception {
		String stepTypeName = step.getStepTypeName();
		long stepNumber = step.getStepNumber();
		List<Long> rollbackStepNumber = step.getRollbackStepNumbers();
		long executionCount = step.getExecutionCount(); 
		Map<String, Parameter> stepParameters = step.getStepParameters();
		List<String> processingLog = step.getProcessingLog();

		StepExec stepExec = getStepExec(stepTypeName);
		StepTransitioner stepTransitioner = getStepTransitioner(stepTypeName); 
		
		return new WerkStep(job, stepTypeName, stepNumber, rollbackStepNumber, 
			executionCount, stepParameters, processingLog, stepExec, stepTransitioner);
	}

	@Override
	public Step createFirstStep(Job job, long stepNumber) throws Exception {
		JobType jobType = werkConfig.getJobTypeForAnyVersion(job.getVersion(), job.getJobTypeName());
		if (jobType == null)
			throw new WerkConfigException(
				String.format("JobType not found [%s] for version [%d]", job.getJobTypeName(), job.getVersion())
			);
			
		createNewStep(job, stepNumber, jobType.getFirstStepTypeName());
		
		return null;
	}

	@Override
	public Step createNewStep(Job job, long stepNumber, String stepType) throws Exception {
		return createNewStep(job, stepNumber, new ArrayList<>(), stepType);
	}

	@Override
	public Step createNewStep(Job job, long stepNumber, List<Long> rollbackStepNumbers, 
			String stepType) throws Exception {
		String stepTypeName = stepType;
		long executionCount = 0; 
		Map<String, Parameter> stepParameters = new HashMap<>();
		List<String> processingLog = new ArrayList<>();

		StepExec stepExec = getStepExec(stepType);
		StepTransitioner stepTransitioner = getStepTransitioner(stepType); 
		
		return new WerkStep(job, stepTypeName, stepNumber, rollbackStepNumbers, 
			executionCount, stepParameters, processingLog, stepExec, stepTransitioner); 
	}

	protected StepType getStepType(String stepTypeName) throws WerkConfigException {
		return werkConfig.getStepType(stepTypeName);
	}
	
	protected StepExec getStepExec(String stepType) throws Exception {
		StepType stepTypeObj = getStepType(stepType);
		StepExec stepExec = stepTypeObj.getStepExecFactory().createStepExec();
		
		return stepExec;
	}
	
	protected StepTransitioner getStepTransitioner(String stepType) throws Exception {
		StepType stepTypeObj = getStepType(stepType);
		StepTransitioner stepTransitioner = stepTypeObj.getStepTransitionerFactory().createStepTransitioner();
		
		return stepTransitioner;
	}
	
	//-------------------------------------------------------
	
	protected void checkParameters(JobType jobType, Map<String, Parameter> parameters) throws WerkConfigException {
		boolean match = false;
		for (List<JobInputParameter> allowedParameters : jobType.getInitParameters()) {
			//Check that all required parameters exist
			boolean allRequiredParametersExist = checkAllRequiredParametersExist(allowedParameters, parameters);
			
			if (allRequiredParametersExist) {
				//Check that no unknown parameters are present
				boolean noUnknownParametersExist = checkNoUnknownParametersExist(allowedParameters, parameters);
				
				if (noUnknownParametersExist)
					match = true;
			}
			
			if (match)
				break;
		}
		
		if (!match)
			throw new WerkConfigException(
					String.format("Can't create JobType [%s] - match for input parameter set not found", 
						jobType.getJobTypeName())
				);
	}
	
	protected boolean isParameterRequired(JobInputParameter ip) {
		if (ip.isOptional())
			return false;
		
		return !(ip instanceof DefaultValueJobInputParameter);
	}
	
	protected Object getParameterValue(Parameter ip) {
		switch (ip.getType()) {
			case LONG: return ((LongParameter)ip).getValue();
			case DOUBLE: return ((DoubleParameter)ip).getValue();
			case BOOL: return ((BoolParameter)ip).getValue();
			case STRING: return ((StringParameter)ip).getValue();
			
			case LIST: return ((ListParameter)ip).getValue();
			case DICTIONARY: return ((DictionaryParameter)ip).getValue();
		}
		
		throw new IllegalArgumentException(
			String.format("Unknown parameter type [%s]", ip.getType())
		);
	}
	
	protected boolean compareParameters(JobInputParameter parameterType, Parameter ip) {
		if (!ip.getType().equals(parameterType.getType()))
			return false;
		
		if (ip instanceof DefaultValueJobInputParameter)
			if (((DefaultValueJobInputParameter)ip).isDefaultValueImmutable())
				if (!((DefaultValueJobInputParameter)ip).getDefaultValue().equals( getParameterValue(ip) ))
					return false;
		
		return true;
	}
	
	protected boolean checkAllRequiredParametersExist(List<JobInputParameter> allowedParameters, 
			Map<String, Parameter> inputParameters) {
		for (JobInputParameter parameterType : allowedParameters) {
			if (isParameterRequired(parameterType)) {
				Parameter ip = inputParameters.get(parameterType.getName());
				if (ip == null)
					return false;
				
				if (!compareParameters(parameterType, ip))
					return false;
			}
		}
		return true;
	}
	
	protected boolean checkNoUnknownParametersExist(List<JobInputParameter> allowedParameters, 
			Map<String, Parameter> inputParameters) {
		Map<String, JobInputParameter> allowedParametersMap = 
				allowedParameters.stream().collect(Collectors.toMap(i -> i.getName(), i -> i));
		
		for (Entry<String, Parameter> ent : inputParameters.entrySet()) {
			String parameterName = ent.getKey();
			Parameter parameter = ent.getValue();
			
			JobInputParameter parameterType = allowedParametersMap.get(parameterName);
			if (parameterType == null)
				return false;
			
			if (!compareParameters(parameterType, parameter))
				return false;
		}
		
		return true;
	}
}
