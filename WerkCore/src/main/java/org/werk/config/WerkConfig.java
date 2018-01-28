package org.werk.config;

import java.util.Collection;
import java.util.Map;

import org.werk.exceptions.WerkConfigException;
import org.werk.meta.JobType;
import org.werk.meta.StepType;

public interface WerkConfig {
	Collection<String> getAllJobTypeNames();
	Collection<Long> getJobTypeVersions(String jobTypeName);

	JobType getJobTypeForAnyVersion(Long version, String jobTypeName);
	JobType getJobTypeForOldVersion(Long version, String jobTypeName);
	JobType getJobTypeLatestVersion(String jobTypeName);
	
	Map<String, StepType> getAllStepTypes();
	StepType getStepType(String stepTypeName);
	
	//-------------------------------------------
	
	void addJobType(JobType jobType) throws WerkConfigException;
	void addStepType(StepType stepType) throws WerkConfigException;
}
