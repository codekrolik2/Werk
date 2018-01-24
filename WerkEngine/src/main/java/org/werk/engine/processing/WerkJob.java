package org.werk.engine.processing;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.pillar.time.interfaces.Timestamp;
import org.werk.processing.jobs.Job;
import org.werk.processing.jobs.JobStatus;
import org.werk.processing.jobs.ReadOnlyStep;
import org.werk.processing.parameters.Parameter;

import lombok.Getter;
import lombok.Setter;

public class WerkJob implements Job {
	@Getter
	protected String jobTypeName;
	@Getter
	protected Optional<String> jobName;
	@Getter @Setter
	protected JobStatus status;
	@Getter @Setter
	protected WerkStep currentStep;
	@Getter
	protected Timestamp nextExecutionTime;
	
	protected Map<String, Parameter> jobInitialParameters;
	
	@Getter
	protected JobContext mainContext;
	@Getter
	protected JobContext tempContext;
	
	public WerkJob(String jobTypeName, Optional<String> jobName, JobStatus status, WerkStep currentStep, 
			Map<String, Parameter> jobInitialParameters, Map<String, Parameter> jobParameters, Timestamp nextExecutionTime) {
		this.jobTypeName = jobTypeName;
		this.jobName = jobName;
		this.status = status;
		this.currentStep = currentStep;
		
		this.jobInitialParameters = jobInitialParameters;
		mainContext = new JobContext(jobParameters);
		this.nextExecutionTime = nextExecutionTime;
	}
	
	//------------------------------------------------
	
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
	
	protected JobContext getCurrentContext() {
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

	//------------------------------------------------
	
	@Override
	public List<ReadOnlyStep> loadProcessingHistory() {
		// TODO: implement load from DB
		return null;
	}

	@Override
	public List<ReadOnlyStep> loadFilteredProcessingHistory(String stepName) {
		// TODO: implement load from DB
		return null;
	}
}
