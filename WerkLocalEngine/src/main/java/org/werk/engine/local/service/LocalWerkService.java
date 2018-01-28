package org.werk.engine.local.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.werk.config.WerkConfig;
import org.werk.data.JobPOJO;
import org.werk.engine.local.LocalJobManager;
import org.werk.meta.JobInitInfo;
import org.werk.meta.JobReviveInfo;
import org.werk.meta.JobType;
import org.werk.meta.OldVersionJobInitInfo;
import org.werk.meta.StepType;
import org.werk.processing.readonly.ReadOnlyJob;
import org.werk.service.WerkService;

public class LocalWerkService implements WerkService<Long> {
	protected LocalJobManager<Long> localJobManager;
	protected WerkConfig<Long> werkConfig;
	
	public LocalWerkService(LocalJobManager<Long> localJobManager, WerkConfig<Long> werkConfig) {
		this.localJobManager = localJobManager;
		this.werkConfig = werkConfig;
	}
	
	@Override
	public Long createJob(JobInitInfo init) throws Exception {
		return localJobManager.createJob(init, Optional.empty());
	}

	@Override
	public Long createOldVersionJob(OldVersionJobInitInfo init) throws Exception {
		return localJobManager.createOldVersionJob(init, Optional.empty());
	}

	@Override
	public void reviveJob(JobReviveInfo<Long> jobReviveInfo) throws Exception {
		localJobManager.reviveJob(jobReviveInfo);
	}

	@Override
	public ReadOnlyJob<Long> getJobAndHistory(Long jobId) throws Exception {
		return localJobManager.getJob(jobId);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Collection<JobPOJO<Long>> getJobs(Optional<Date> from, Optional<Date> to, Optional<Set<String>> jobTypes,
			Optional<Collection<Long>> jobIds) throws Exception {
		return (Collection<JobPOJO<Long>>)(List)localJobManager.getJobs(jobIds.get());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Collection<JobPOJO<Long>> getChildJobs(Long jobId) throws Exception {
		return (Collection<JobPOJO<Long>>)(List)localJobManager.getAllChildJobs(jobId);
	}

	@Override
	public Collection<ReadOnlyJob<Long>> getChildJobsAndHistory(Long jobId) throws Exception {
		return localJobManager.getAllChildJobs(jobId);
	}

	//-------------------------------------------------------------
	
	@Override
	public Collection<JobType> getJobTypes() {
		return werkConfig.getAllJobTypes();
	}

	@Override
	public JobType getJobType(String jobTypeName, Optional<Long> version) {
		if (version.isPresent())
			return werkConfig.getJobTypeLatestVersion(jobTypeName);
		else
			return werkConfig.getJobTypeForAnyVersion(version.get(), jobTypeName);
	}

	@Override
	public Collection<StepType<Long>> getStepTypesForJob(String jobTypeName, Optional<Long> version) {
		JobType jobType = getJobType(jobTypeName, version);
		
		List<StepType<Long>> stepTypes = new ArrayList<>();
		for (String stepTypeName : jobType.getStepTypes()) {
			StepType<Long> stepType = getStepType(stepTypeName);
			stepTypes.add(stepType);
		}
		
		return stepTypes;
	}

	@Override
	public StepType<Long> getStepType(String stepTypeName) {
		return werkConfig.getStepType(stepTypeName);
	}
}
