package org.werk.jobs;

import java.util.List;
import java.util.Map;

import org.werk.parameters.interfaces.Parameter;
import org.werk.steps.Step;

public interface Job {
	String getJobTypeName();

	//Processing
	/**
	 * @return Read-only map of init parameters
	 */
	Map<String, Parameter> getJobInitialParameters();
	Parameter getJobInitialParameter(String parameterName);
	
	/**
	 * @return Read-only map of job parameters
	 */
	Map<String, Parameter> getJobParameters();
	Parameter getJobParameter(String parameterName);
	Parameter removeJobParameter(String parameterName);
	void putJobParameter(String parameterName, Parameter parameter);
	
	JobStatus getStatus();
	
	Step getCurrentStep();
	List<Step> getProcessingHistory();
	
	//-----------------------------------------------
	
	//Transitions
	void nextStep(String stepName);
	void rollback(String stepName);
	void finish();
	void fail();
}
