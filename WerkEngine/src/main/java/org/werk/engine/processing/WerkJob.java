package org.werk.engine.processing;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.werk.jobs.Job;
import org.werk.jobs.JobStatus;
import org.werk.jobs.ReadOnlyStep;
import org.werk.parameters.interfaces.Parameter;
import org.werk.steps.Step;

import lombok.Getter;
import lombok.Setter;

public class WerkJob implements Job {
	@Getter
	protected String jobTypeName;
	@Getter @Setter
	protected JobStatus status;
	@Getter @Setter
	protected Step currentStep;
	
	protected Map<String, Parameter> jobInitialParameters;
	
	protected JobContext mainContext;
	protected JobContext tempContext;
	
	public WerkJob(String jobTypeName, JobStatus status, Step currentStep, 
			Map<String, Parameter> jobInitialParameters, Map<String, Parameter> jobParameters) {
		this.jobTypeName = jobTypeName;
		this.status = status;
		this.currentStep = currentStep;
		
		this.jobInitialParameters = jobInitialParameters;
		mainContext = new JobContext(jobParameters);
	}
	
	//------------------------------------------------
	
	public void setTempContext(JobContext tempContext) {
		this.tempContext = tempContext;
	}
	
	public void setMainContext(JobContext mainContext) {
		this.mainContext = mainContext;
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
		return Collections.unmodifiableMap(getCurrentContext().getJobParameters());
	}

	@Override
	public Parameter getJobParameter(String parameterName) {
		return getCurrentContext().getJobParameter(parameterName);
	}

	@Override
	public Parameter removeJobParameter(String parameterName) {
		return getCurrentContext().removeJobParameter(parameterName);
	}

	@Override
	public void putJobParameter(String parameterName, Parameter parameter) {
		getCurrentContext().putJobParameter(parameterName, parameter);
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
