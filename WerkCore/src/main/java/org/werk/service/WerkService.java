package org.werk.service;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.pillar.time.interfaces.Timestamp;
import org.werk.meta.JobInitInfo;
import org.werk.meta.JobRestartInfo;
import org.werk.meta.JobType;
import org.werk.meta.StepType;
import org.werk.meta.VersionJobInitInfo;
import org.werk.processing.readonly.ReadOnlyJob;

public interface WerkService<J> {
	//JOB CREATION/RESTART
	J createJob(JobInitInfo init) throws Exception;
	J createJobOfVersion(VersionJobInitInfo init) throws Exception;
	void restartJob(JobRestartInfo<J> jobRestartInfo) throws Exception;
	
	//JOB RETRIEVAL
	ReadOnlyJob<J> getJobAndHistory(J jobId) throws Exception;
	JobCollection getJobs(Optional<Timestamp> from, Optional<Timestamp> to, 
			Optional<Map<String, Long>> jobTypesAndVersions, Optional<Collection<J>> parentJobIds, 
			Optional<Collection<J>> jobIds, Optional<Set<String>> currentStepTypes, 
			Optional<PageInfo> pageInfo) throws Exception;
	
	//JOB METADATA RETRIEVAL
	Collection<JobType> getJobTypes();
	JobType getJobType(String jobTypeName, Optional<Long> version);
	
	Collection<StepType<J>> getAllStepTypes();
	Collection<StepType<J>> getStepTypesForJob(String jobTypeName, Optional<Long> version);
	StepType<J> getStepType(String stepTypeName);
}