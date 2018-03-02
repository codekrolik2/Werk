package org.werk.engine.local;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.pillar.time.interfaces.Timestamp;
import org.werk.config.WerkConfig;
import org.werk.data.JobPOJO;
import org.werk.meta.JobInitInfo;
import org.werk.meta.JobRestartInfo;
import org.werk.meta.JobType;
import org.werk.meta.StepType;
import org.werk.meta.VersionJobInitInfo;
import org.werk.processing.jobs.JobStatus;
import org.werk.processing.readonly.ReadOnlyJob;
import org.werk.service.JobCollection;
import org.werk.service.PageInfo;
import org.werk.service.WerkService;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class LocalWerkService implements WerkService<Long> {
	protected LocalJobManager<Long> localJobManager;
	protected WerkConfig<Long> werkConfig;
	
	@Override
	public Long createJob(JobInitInfo init) throws Exception {
		return localJobManager.createJob(init, Optional.empty());
	}

	@Override
	public Long createJobOfVersion(VersionJobInitInfo init) throws Exception {
		return localJobManager.createJobOfVersion(init, Optional.empty());
	}

	@Override
	public void restartJob(JobRestartInfo<Long> jobRestartInfo) throws Exception {
		localJobManager.restartJob(jobRestartInfo);
	}

	@Override
	public ReadOnlyJob<Long> getJobAndHistory(Long jobId) throws Exception {
		return localJobManager.getJob(jobId);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public JobCollection getJobs(Optional<Timestamp> from, Optional<Timestamp> to, 
			Optional<Timestamp> fromExec, Optional<Timestamp> toExec, Optional<Map<String, Long>> jobTypesAndVersions,
			Optional<Collection<Long>> parentJobIds, Optional<Collection<Long>> jobIds, Optional<Set<String>> currentStepTypes, 
			Optional<Set<JobStatus>> jobStatuses , Optional<PageInfo> pageInfo) throws Exception {
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
		
		if (fromExec.isPresent())
			jobs = jobs.stream().filter(a -> a.getNextExecutionTime().compareTo(fromExec.get()) >= 0).collect(Collectors.toList());
		
		if (toExec.isPresent())
			jobs = jobs.stream().filter(a -> a.getNextExecutionTime().compareTo(toExec.get()) <= 0).collect(Collectors.toList());
		
		if (from.isPresent())
			jobs = jobs.stream().filter(a -> a.getCreationTime().compareTo(from.get()) >= 0).collect(Collectors.toList());
		
		if (to.isPresent())
			jobs = jobs.stream().filter(a -> a.getCreationTime().compareTo(to.get()) <= 0).collect(Collectors.toList());
		
		if (jobTypesAndVersions.isPresent())
			jobs = jobs.stream().
				filter(a ->
					jobTypesAndVersions.get().containsKey(a.getJobTypeName())
					&&
					(
						jobTypesAndVersions.get().get(a.getJobTypeName()).equals(a.getVersion())
						||
						(jobTypesAndVersions.get().get(a.getJobTypeName()).compareTo(0L) <= 0)
					)
				).
				collect(Collectors.toList());
		
		if (currentStepTypes.isPresent())
			jobs = jobs.stream().
				filter(a -> currentStepTypes.get().contains(((LocalWerkJob)a).getCurrentStep().getStepTypeName())).
				collect(Collectors.toList());
		
		if (jobStatuses.isPresent())
			jobs = jobs.stream().
				filter(a -> jobStatuses.get().contains(a.getStatus())).
				collect(Collectors.toList());
		
		int jobCount = jobs.size();
		if (pageInfo.isPresent()) {
			long itemsPerPage = pageInfo.get().getItemsPerPage();
			long pageNumber = pageInfo.get().getPageNumber();
			
			jobs = jobs.stream().skip(pageNumber*itemsPerPage).limit(itemsPerPage).collect(Collectors.toList());
		}
		
		return new JobCollection(pageInfo, jobs, jobCount);
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

	@Override
	public Collection<StepType<Long>> getAllStepTypes() {
		return werkConfig.getAllStepTypes().values();
	}

	@Override
	public void jobsAdded() {
		// LocalWerkService has no job loader to notify
	}

	@Override
	public JSONObject getServerInfo() {
		JSONObject info = new JSONObject();
		info.put("server", "In-Process");
		return info;
	}

	@Override
	public Collection<JobType> getJobTypesForStep(String stepTypeName) {
		return werkConfig.getAllJobTypes();
	}
}
