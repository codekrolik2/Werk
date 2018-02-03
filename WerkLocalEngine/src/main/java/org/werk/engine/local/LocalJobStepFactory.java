package org.werk.engine.local;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.pillar.time.interfaces.TimeProvider;
import org.pillar.time.interfaces.Timestamp;
import org.werk.config.WerkConfig;
import org.werk.data.JobPOJO;
import org.werk.data.StepPOJO;
import org.werk.engine.JobStepFactory;
import org.werk.engine.json.JoinResultSerializer;
import org.werk.engine.json.ParameterUtils;
import org.werk.engine.processing.WerkStep;
import org.werk.exceptions.WerkConfigException;
import org.werk.exceptions.WerkException;
import org.werk.meta.JobType;
import org.werk.meta.StepType;
import org.werk.meta.inputparameters.DefaultValueJobInputParameter;
import org.werk.meta.inputparameters.EnumJobInputParameter;
import org.werk.meta.inputparameters.JobInputParameter;
import org.werk.meta.inputparameters.RangeJobInputParameter;
import org.werk.processing.jobs.Job;
import org.werk.processing.jobs.JobStatus;
import org.werk.processing.jobs.JoinStatusRecord;
import org.werk.processing.parameters.Parameter;
import org.werk.processing.steps.Step;
import org.werk.processing.steps.StepExec;
import org.werk.processing.steps.StepProcessingLogRecord;
import org.werk.processing.steps.Transitioner;

public abstract class LocalJobStepFactory<J> implements JobStepFactory<J> {
	protected WerkConfig<J> werkConfig;
	protected TimeProvider timeProvider;
	protected LocalJobManager<J> localJobManager;
	protected JoinResultSerializer<J> joinResultSerializer;
	
	public LocalJobStepFactory(WerkConfig<J> werkConfig, TimeProvider timeProvider, LocalJobManager<J> localJobManager,
			JoinResultSerializer<J> joinResultSerializer) {
		this.werkConfig = werkConfig;
		this.timeProvider = timeProvider;
		this.localJobManager = localJobManager;
		this.joinResultSerializer = joinResultSerializer;
	}
	
	protected abstract J getNextJobId();
	
	protected void fillParameters(List<JobInputParameter> parameterSet, Map<String, Parameter> jobInitialParameters) {
		for (JobInputParameter parameter : parameterSet) {
			if (!jobInitialParameters.containsKey(parameter.getName())) {
				if (parameter instanceof DefaultValueJobInputParameter) {
					Parameter defaultValue = ((DefaultValueJobInputParameter)parameter).getDefaultValue();
					jobInitialParameters.put(parameter.getName(), defaultValue);
				}
			}
		}
	}
	
	protected boolean parametersEqual(Parameter p1, Parameter p2) {
		if (p1.getType() != p2.getType())
			return false;
		return ParameterUtils.getParameterValue(p1).equals( ParameterUtils.getParameterValue(p2) );
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected int parametersCompare(Parameter p1, Parameter p2) throws WerkException {
		if (p1.getType() != p2.getType())
			throw new WerkException(
				String.format("Can't compare parameters of different types [%s] [%s]", p1.getType(), p2.getType())
			);
		
		return ((Comparable)ParameterUtils.getParameterValue(p1)).compareTo( ParameterUtils.getParameterValue(p2) );
	}
	
	protected void checkRangeAndEnumParameters(List<JobInputParameter> parameterSet, 
			Map<String, Parameter> jobInitialParameters) throws WerkException {
		for (JobInputParameter parameter : parameterSet) {
			if (jobInitialParameters.containsKey(parameter.getName())) {
				Parameter jobPrm = jobInitialParameters.get(parameter.getName());
				
				if (parameter instanceof EnumJobInputParameter) {
					boolean match = false;
					for (Parameter p : ((EnumJobInputParameter)parameter).getValues()) {
						if (parametersEqual(p, jobPrm)) { 
							match = true;
							break;
						}
					}
					
					if (((EnumJobInputParameter)parameter).isProhibitValues()) {
						if (match)
							throw new WerkException(
								String.format("Enum value is prohibited for value [%s], param [%s] ", 
										ParameterUtils.getParameterValue(jobPrm), parameter)
							);
					} else {
						if (!match)
							throw new WerkException(
								String.format("Enum match not found for value [%s], param [%s]", 
										ParameterUtils.getParameterValue(jobPrm), parameter)
							);
					}
				} else if (parameter instanceof RangeJobInputParameter) {
					Parameter start = ((RangeJobInputParameter)parameter).getStart();
					Parameter end = ((RangeJobInputParameter)parameter).getEnd();
					
					if (parametersCompare(start, end) > 0) {
						Parameter tmp = start;
						start = end;
						end = tmp;
					}
					
					boolean startConstraint;
					if (((RangeJobInputParameter)parameter).isStartInclusive())
						startConstraint = (parametersCompare(start, jobPrm) <= 0);
					else
						startConstraint = (parametersCompare(start, jobPrm) < 0);
					
					boolean endConstraint;
					if (((RangeJobInputParameter)parameter).isEndInclusive())
						endConstraint = (parametersCompare(jobPrm, end) <= 0);
					else
						endConstraint = (parametersCompare(jobPrm, end) < 0);
					
					if (startConstraint && endConstraint) {
						if (((RangeJobInputParameter)parameter).isProhibitRange())
							throw new WerkException(
								String.format("Parameter value in prohibited range [%s - %s]; name [%s], value [%s]", 
										ParameterUtils.getParameterValue(start), ParameterUtils.getParameterValue(end), 
										parameter.getName(), ParameterUtils.getParameterValue(jobPrm))
							);
					} else {
						if (!((RangeJobInputParameter)parameter).isProhibitRange())
							throw new WerkException(
								String.format("Parameter value outside of allowed range [%s - %s]; name [%s], value [%s]", 
										ParameterUtils.getParameterValue(start), ParameterUtils.getParameterValue(end), 
										parameter.getName(), ParameterUtils.getParameterValue(jobPrm))
							);
					}
				}
			}
		}
	}
	
	@Override
	public Job<J> createNewJob(String jobTypeName, Map<String, Parameter> jobInitialParameters, 
			Optional<String> jobName, Optional<J> parentJob) throws Exception {
		JobType jobType = werkConfig.getJobTypeLatestVersion(jobTypeName);
		if (jobType == null)
			throw new WerkConfigException(
				String.format("JobType not found [%s]", jobTypeName)
			);
		
		//Check initial parameters
		List<JobInputParameter> parameterSet = findMatchingParameterSet(jobType, jobInitialParameters);
		
		//Check enum and range parameters
		checkRangeAndEnumParameters(parameterSet, jobInitialParameters);
		
		//Fill default parameters
		fillParameters(parameterSet, jobInitialParameters);
		
		long version = jobType.getVersion();
		JobStatus status = JobStatus.UNDEFINED;
		Map<String, Parameter> jobParameters = new HashMap<>(jobInitialParameters);
		Timestamp nextExecutionTime = timeProvider.getCurrentTime();
		Optional<JoinStatusRecord<J>> joinStatusRecord = Optional.empty();
		
		return new LocalWerkJob<J>(getNextJobId(), jobType, version, jobName, status, 
				jobInitialParameters, jobParameters, nextExecutionTime, joinStatusRecord, parentJob,
				localJobManager, joinResultSerializer);
	}

	@Override
	public Job<J> createOldVersionJob(String jobTypeName, long oldVersion, Map<String, Parameter> jobInitialParameters, 
			Optional<String> jobName, Optional<J> parentJob) throws Exception {
		JobType jobType = werkConfig.getJobTypeForOldVersion(oldVersion, jobTypeName);
		if (jobType == null)
			throw new WerkConfigException(
				String.format("JobType not found [%s] for version [%d]", jobTypeName, oldVersion)
			);
		
		//Check initial parameters
		List<JobInputParameter> parameterSet = findMatchingParameterSet(jobType, jobInitialParameters);

		//Check enum and range parameters
		checkRangeAndEnumParameters(parameterSet, jobInitialParameters);
		
		//Fill default parameters
		fillParameters(parameterSet, jobInitialParameters);
		
		long version = jobType.getVersion();
		JobStatus status = JobStatus.UNDEFINED;
		Map<String, Parameter> jobParameters = new HashMap<>(jobInitialParameters);
		Timestamp nextExecutionTime = timeProvider.getCurrentTime();
		Optional<JoinStatusRecord<J>> joinStatusRecord = Optional.empty();
		
		return new LocalWerkJob<J>(getNextJobId(), jobType, version, jobName, status, 
				jobInitialParameters, jobParameters, nextExecutionTime, joinStatusRecord, parentJob,
				localJobManager, joinResultSerializer);
	}

	@Override
	public Job<J> createJob(JobPOJO<J> job) throws Exception {
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
		Optional<JoinStatusRecord<J>> joinStatusRecord = job.getJoinStatusRecord();
		Optional<J> parentJob = job.getParentJobId();
		
		return new LocalWerkJob<J>(((LocalWerkJob<J>)job).getJobId(), jobType, version, jobName, status, 
				jobInitialParameters, jobParameters, nextExecutionTime, joinStatusRecord, parentJob,
				localJobManager, joinResultSerializer);
	}

	//---------------------------------------------
	
	@Override
	public Step<J> createStep(Job<J> job, StepPOJO step) throws Exception {
		String stepTypeName = step.getStepTypeName();
		StepType<J> stepType = werkConfig.getStepType(stepTypeName);
		
		long stepNumber = step.getStepNumber();
		List<Long> rollbackStepNumber = step.getRollbackStepNumbers();
		long executionCount = step.getExecutionCount(); 
		Map<String, Parameter> stepParameters = step.getStepParameters();
		List<StepProcessingLogRecord> processingLog = step.getProcessingLog();

		StepExec<J> stepExec = getStepExec(stepTypeName);
		Transitioner<J> stepTransitioner = getStepTransitioner(stepTypeName); 
		
		return new WerkStep<J>(job, stepType, (job.getStatus() == JobStatus.ROLLING_BACK), stepNumber, rollbackStepNumber, 
				executionCount, stepParameters, processingLog, stepExec, stepTransitioner, timeProvider);
	}

	@Override
	public Step<J> createFirstStep(Job<J> job, long stepNumber) throws Exception {
		JobType jobType = werkConfig.getJobTypeForAnyVersion(job.getVersion(), job.getJobTypeName());
		if (jobType == null)
			throw new WerkConfigException(
				String.format("JobType not found [%s] for version [%d]", job.getJobTypeName(), job.getVersion())
			);
			
		return createNewStep(job, stepNumber, jobType.getFirstStepTypeName());
	}

	@Override
	public Step<J> createNewStep(Job<J> job, long stepNumber, String stepType) throws Exception {
		return createNewStep(job, stepNumber, new ArrayList<>(), stepType);
	}

	@Override
	public Step<J> createNewStep(Job<J> job, long stepNumber, List<Long> rollbackStepNumbers, 
			String stepType) throws Exception {
		String stepTypeName = stepType;
		StepType<J> stepTypeObj = werkConfig.getStepType(stepTypeName);
		
		long executionCount = 0; 
		Map<String, Parameter> stepParameters = new HashMap<>();
		List<StepProcessingLogRecord> processingLog = new ArrayList<>();

		StepExec<J> stepExec = getStepExec(stepType);
		Transitioner<J> stepTransitioner = getStepTransitioner(stepType); 
		
		return new WerkStep<J>(job, stepTypeObj, (job.getStatus() == JobStatus.ROLLING_BACK), stepNumber, rollbackStepNumbers,
				executionCount, stepParameters, processingLog, stepExec, stepTransitioner, timeProvider); 
	}

	protected StepType<J> getStepType(String stepTypeName) throws WerkConfigException {
		return werkConfig.getStepType(stepTypeName);
	}
	
	protected StepExec<J> getStepExec(String stepType) throws Exception {
		StepType<J> stepTypeObj = getStepType(stepType);
		StepExec<J> stepExec = stepTypeObj.getStepExecFactory().createStepExec();
		
		return stepExec;
	}
	
	protected Transitioner<J> getStepTransitioner(String stepType) throws Exception {
		StepType<J> stepTypeObj = getStepType(stepType);
		Transitioner<J> stepTransitioner = stepTypeObj.getStepTransitionerFactory().createStepTransitioner();
		
		return stepTransitioner;
	}
	
	//-------------------------------------------------------
	
	protected List<JobInputParameter> findMatchingParameterSet(JobType jobType, Map<String, Parameter> parameters) throws WerkConfigException {
		boolean match = false;
		for (List<JobInputParameter> allowedParameters : jobType.getInitParameters().values()) {
			//Check that all required parameters exist
			boolean allRequiredParametersExist = checkAllRequiredParametersExist(allowedParameters, parameters);
			
			if (allRequiredParametersExist) {
				//Check that no unknown parameters are present
				boolean noUnknownParametersExist = checkNoUnknownParametersExist(allowedParameters, parameters);
				
				if (noUnknownParametersExist)
					match = true;
			}
			
			if (match)
				return allowedParameters;
		}
		
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
	
	protected boolean compareParameters(JobInputParameter parameterType, Parameter ip) {
		if (!ip.getType().equals(parameterType.getType()))
			return false;
		
		if (ip instanceof DefaultValueJobInputParameter)
			if (((DefaultValueJobInputParameter)ip).isDefaultValueImmutable())
				if (!ParameterUtils.getParameterValue(((DefaultValueJobInputParameter)ip).getDefaultValue()
						).equals( ParameterUtils.getParameterValue(ip) ))
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
