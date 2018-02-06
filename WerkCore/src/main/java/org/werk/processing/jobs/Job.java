package org.werk.processing.jobs;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.werk.meta.JobInitInfo;
import org.werk.meta.JobReviveInfo;
import org.werk.meta.JobType;
import org.werk.meta.OldVersionJobInitInfo;
import org.werk.processing.parameters.Parameter;
import org.werk.processing.readonly.ReadOnlyJob;
import org.werk.processing.steps.JoinResult;
import org.werk.processing.steps.Step;

public interface Job<J> extends ReadOnlyJob<J> {
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
	
	Long getLongParameter(String parameterName);
	void putLongParameter(String parameterName, Long value);
	Double getDoubleParameter(String parameterName);
	void putDoubleParameter(String parameterName, Double value);
	Boolean getBoolParameter(String parameterName);
	void putBoolParameter(String parameterName, Boolean value);
	String getStringParameter(String parameterName);
	void putStringParameter(String parameterName, String value);
	Map<String, Parameter> getDictionaryParameter(String parameterName);
	void putDictionaryParameter(String parameterName, Map<String, Parameter> value);
	List<Parameter> getListParameter(String parameterName);
	void putListParameter(String parameterName, List<Parameter> value);
	
	//JobStatus getStatus();
	//void setStatus(JobStatus status);
	
	Step<J> getCurrentStep();
	
	void openTempContext();
	void openTempContextAndRemap(Object obj);
	void commitTempContext();
	void rollbackTempContext();
	
	//-------------------------------------------------------------------
	
	J fork(JobInitInfo jobInitInfo) throws Exception;
	J forkOldVersion(OldVersionJobInitInfo jobInitInfo) throws Exception;
	
	void revive(JobReviveInfo<J> jobReviveInfo) throws Exception;
	
	List<J> getCreatedJobs();
	
	String joinResultToStr(JoinResult<J> joinResult);
	JoinResult<J> strToJoinResult(String joinResultStr);
	
	ReadOnlyJob<J> loadJob(J jobId) throws Exception;
	List<ReadOnlyJob<J>> loadJobs(Collection<J> jobIds) throws Exception;
	List<ReadOnlyJob<J>> loadAllChildJobs() throws Exception;
	List<ReadOnlyJob<J>> loadChildJobsOfTypes(Set<String> jobTypes) throws Exception;
	
	//-------------------------------------------------------------------
	
	JobType getJobType();
}
