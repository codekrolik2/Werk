package org.werk.processing.jobs;

import java.util.List;

import org.werk.data.JobPOJO;
import org.werk.processing.parameters.Parameter;
import org.werk.processing.steps.Step;

public interface Job extends JobPOJO {
	//String getJobTypeName();
	
	//Processing
	/**
	 * @return Read-only map of init parameters
	 */
	//Map<String, Parameter> getJobInitialParameters();
	Parameter getJobInitialParameter(String parameterName);
	
	/**
	 * @return Read-only map of job parameters
	 */
	//Map<String, Parameter> getJobParameters();
	Parameter getJobParameter(String parameterName);
	Parameter removeJobParameter(String parameterName);
	void putJobParameter(String parameterName, Parameter parameter);
	
	//JobStatus getStatus();
	
	Step getCurrentStep();
	
	List<ReadOnlyStep> loadProcessingHistory();
	List<ReadOnlyStep> loadFilteredProcessingHistory(String stepName);

	void openTempContext();
	void openTempContextAndRemap(Object obj);
	void commitTempContext();
}
