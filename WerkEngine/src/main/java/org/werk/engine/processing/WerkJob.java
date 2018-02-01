package org.werk.engine.processing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.pillar.time.interfaces.Timestamp;
import org.werk.meta.JobType;
import org.werk.processing.jobs.Job;
import org.werk.processing.jobs.JobStatus;
import org.werk.processing.jobs.JoinStatusRecord;
import org.werk.processing.parameters.Parameter;

import lombok.Getter;
import lombok.Setter;

public abstract class WerkJob<J> implements Job<J> {
	@Getter
	protected JobType jobType;
	@Getter
	protected Optional<J> parentJobId;
	@Getter
	protected long version;
	@Getter
	protected Optional<String> jobName;
	@Getter @Setter
	protected JobStatus status;
	@Getter @Setter
	protected WerkStep<J> currentStep;
	@Getter
	protected Timestamp nextExecutionTime;
	
	protected Map<String, Parameter> jobInitialParameters;
	
	@Getter
	protected JobContext<J> mainContext;
	@Getter
	protected JobContext<J> tempContext;
	@Getter @Setter
	protected Optional<JoinStatusRecord<J>> joinStatusRecord;
	
	protected List<J> createdJobs;
	
	public WerkJob(JobType jobType, long version, Optional<String> jobName, JobStatus status, 
			Map<String, Parameter> jobInitialParameters, Map<String, Parameter> jobParameters, Timestamp nextExecutionTime,
			Optional<JoinStatusRecord<J>> joinStatusRecord, Optional<J> parentJobId) {
		this.jobType = jobType;
		this.version = version;
		this.jobName = jobName;
		this.status = status;
		this.joinStatusRecord = joinStatusRecord;
		this.parentJobId = parentJobId;
		
		this.jobInitialParameters = jobInitialParameters;
		mainContext = new JobContext<J>(jobParameters);
		this.nextExecutionTime = nextExecutionTime;
		
		createdJobs = new ArrayList<>();
	}
	
	//------------------------------------------------
	
	@Override
	public String getJobTypeName() {
		return jobType.getJobTypeName();
	}
	
	@Override
	public void openTempContext() {
		if (tempContext != null)
			throw new IllegalStateException("Temp context already opened");
		
		tempContext = mainContext.cloneContext();
		currentStep.openTempContext();
	}
	
	@Override
	public void openTempContextAndRemap(Object obj) {
		openTempContext();
		ContextParameterMapper.remapParameters(tempContext, currentStep.getTempContext(), obj);
	}
	
	@Override
	public void commitTempContext() {
		currentStep.commitTempContext();
		mainContext = tempContext;
		tempContext = null;
	}

	@Override
	public void rollbackTempContext() {
		currentStep.rollbackTempContext();
		
		if (tempContext == null)
			throw new IllegalStateException("Temp context not opened");
		
		tempContext = null;
	}
	
	public JobContext<J> getCurrentContext() {
		return tempContext == null ? mainContext : tempContext;
	}
	
	//------------------------------------------------
	
	@Override
	public Map<String, Parameter> getJobInitialParameters() {
		return Collections.unmodifiableMap(jobInitialParameters);
	}

	@Override
	public Parameter getJobInitialParameter(String parameterName) {
		return jobInitialParameters.get(parameterName);
	}

	@Override
	public Map<String, Parameter> getJobParameters() {
		return getCurrentContext().getParameters();
	}

	@Override
	public Parameter getJobParameter(String parameterName) {
		return getCurrentContext().getParameter(parameterName);
	}

	@Override
	public Parameter removeJobParameter(String parameterName) {
		return getCurrentContext().removeParameter(parameterName);
	}

	@Override
	public void putJobParameter(String parameterName, Parameter parameter) {
		getCurrentContext().putParameter(parameterName, parameter);
	}

	@Override
	public Long getLongParameter(String parameterName) {
		return getCurrentContext().getLongParameter(parameterName);
	}

	@Override
	public void putLongParameter(String parameterName, Long value) {
		getCurrentContext().putLongParameter(parameterName, value);
	}
	
	@Override
	public Double getDoubleParameter(String parameterName) {
		return getCurrentContext().getDoubleParameter(parameterName);
	}

	@Override
	public void putDoubleParameter(String parameterName, Double value) {
		getCurrentContext().putDoubleParameter(parameterName, value);
	}
	
	@Override
	public Boolean getBoolParameter(String parameterName) {
		return getCurrentContext().getBoolParameter(parameterName);
	}

	@Override
	public void putBoolParameter(String parameterName, Boolean value) {
		getCurrentContext().putBoolParameter(parameterName, value);
	}
	
	@Override
	public String getStringParameter(String parameterName) {
		return getCurrentContext().getStringParameter(parameterName);
	}

	@Override
	public void putStringParameter(String parameterName, String value) {
		getCurrentContext().putStringParameter(parameterName, value);
	}
	
	@Override
	public Map<String, Parameter> getDictionaryParameter(String parameterName) {
		return getCurrentContext().getDictionaryParameter(parameterName);
	}

	@Override
	public void putDictionaryParameter(String parameterName, Map<String, Parameter> value) {
		getCurrentContext().putDictionaryParameter(parameterName, value);
	}
	
	@Override
	public List<Parameter> getListParameter(String parameterName) {
		return getCurrentContext().getListParameter(parameterName);
	}

	@Override
	public void putListParameter(String parameterName, List<Parameter> value) {
		getCurrentContext().putListParameter(parameterName, value);
	}
	
	protected void addCreatedJob(J jobId) {
		getCurrentContext().addCreatedJob(jobId);
	}
	
	//------------------------------------------------

	@Override
	public List<J> getCreatedJobs() {
		return Collections.unmodifiableList(createdJobs);
	}
	
	//------------------------------------------------
}
