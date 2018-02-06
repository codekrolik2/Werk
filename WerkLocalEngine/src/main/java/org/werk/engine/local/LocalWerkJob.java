package org.werk.engine.local;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.pillar.time.interfaces.Timestamp;
import org.werk.data.StepPOJO;
import org.werk.engine.json.JoinResultSerializer;
import org.werk.engine.processing.WerkJob;
import org.werk.meta.JobInitInfo;
import org.werk.meta.JobReviveInfo;
import org.werk.meta.JobType;
import org.werk.meta.OldVersionJobInitInfo;
import org.werk.processing.jobs.JobStatus;
import org.werk.processing.jobs.JoinStatusRecord;
import org.werk.processing.parameters.Parameter;
import org.werk.processing.readonly.ReadOnlyJob;

import lombok.Getter;
import lombok.Setter;

public class LocalWerkJob<J> extends WerkJob<J> {
	@Getter
	protected J jobId;
	@Setter
	protected Collection<StepPOJO> processingHistory;
	
	protected LocalJobManager<J> jobManager;
	
	public LocalWerkJob(J jobId, JobType jobType, long version, Optional<String> jobName, JobStatus status,
			Map<String, Parameter> jobInitialParameters, Map<String, Parameter> jobParameters, int stepCount,
			Timestamp nextExecutionTime, Optional<JoinStatusRecord<J>> joinStatusRecord, Optional<J> parentJobId,
			LocalJobManager<J> jobManager, JoinResultSerializer<J> joinResultSerializer) {
		super(jobType, version, jobName, status, jobInitialParameters, jobParameters, nextExecutionTime, 
				joinStatusRecord, parentJobId, stepCount, joinResultSerializer);
		this.jobId = jobId;
		processingHistory = new ArrayList<>();
		this.parentJobId = parentJobId;
		this.jobManager = jobManager;
		this.joinResultSerializer = joinResultSerializer;
	}
	
	@Override
	public Collection<StepPOJO> getProcessingHistory() {
		return processingHistory;
	}

	@Override
	public Collection<StepPOJO> getFilteredHistory(String stepTypeName) {
		return processingHistory.stream().
				filter(a -> a.getStepTypeName().equals(stepTypeName)).collect(Collectors.toList());
	}

	@Override
	public StepPOJO getStep(long stepNumber) {
		for (StepPOJO historyStep : processingHistory)
			if (historyStep.getStepNumber() == stepNumber)
				return historyStep;
		
		return null;
	}
	
	//----------------------------------------------
	
	@Override
	public J fork(JobInitInfo jobInitInfo) throws Exception {
		return jobManager.createJob(jobInitInfo, Optional.of(getJobId()));
	}

	@Override
	public J forkOldVersion(OldVersionJobInitInfo jobInitInfo) throws Exception {
		return jobManager.createOldVersionJob(jobInitInfo, Optional.of(getJobId()));
	}

	@Override
	public void revive(JobReviveInfo<J> jobReviveInfo) throws Exception {
		jobManager.reviveJob(jobReviveInfo);
	}

	//----------------------------------------------

	@Override
	public ReadOnlyJob<J> loadJob(J jobId) {
		return jobManager.getJob(jobId);
	}

	@Override
	public List<ReadOnlyJob<J>> loadJobs(Collection<J> jobIds) {
		return jobManager.getJobs(jobIds);
	}

	@Override
	public List<ReadOnlyJob<J>> loadAllChildJobs() {
		return jobManager.getAllChildJobs(getJobId());
	}

	@Override
	public List<ReadOnlyJob<J>> loadChildJobsOfTypes(Set<String> jobTypes) {
		return jobManager.getChildJobsOfTypes(getJobId(), jobTypes);
	}
}
