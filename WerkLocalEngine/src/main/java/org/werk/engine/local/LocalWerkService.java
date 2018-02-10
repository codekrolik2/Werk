package org.werk.engine.local;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.pillar.time.interfaces.Timestamp;
import org.werk.config.WerkConfig;
import org.werk.data.JobPOJO;
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
	public Collection<JobPOJO<Long>> getJobs(Optional<Timestamp> from, Optional<Timestamp> to, Optional<Set<String>> jobTypes,
			Optional<Collection<Long>> parentJobIds, Optional<Collection<Long>> jobIds) throws Exception {
		Collection<JobPOJO<Long>> jobs;
		if (parentJobIds.isPresent()) {
			jobs = (Collection<JobPOJO<Long>>)(Collection)localJobManager.getAllChildJobs(parentJobIds.get());
			if (jobIds.isPresent()) {
				Set<Long> jobIdSet = new HashSet<>(jobIds.get());
				jobs = jobs.stream().filter(a -> jobIdSet.contains(a.getJobId())).collect(Collectors.toList());
			}
		} else if (jobIds.isPresent())
			jobs = (Collection<JobPOJO<Long>>)(List)localJobManager.getJobs(jobIds.get());
		else
			jobs = (Collection<JobPOJO<Long>>)(List)localJobManager.getAllJobs();
		
		if (from.isPresent())
			jobs = jobs.stream().filter(a -> a.getNextExecutionTime().compareTo(from.get()) >= 0).collect(Collectors.toList());
		
		if (to.isPresent())
			jobs = jobs.stream().filter(a -> a.getNextExecutionTime().compareTo(to.get()) <= 0).collect(Collectors.toList());
		
		if (jobTypes.isPresent())
			jobs = jobs.stream().filter(a -> jobTypes.get().contains(a.getJobTypeName())).collect(Collectors.toList());
		
		return jobs;
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
