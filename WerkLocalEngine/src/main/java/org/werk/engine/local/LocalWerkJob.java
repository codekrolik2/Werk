package org.werk.engine.local;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.pillar.time.interfaces.Timestamp;
import org.werk.data.StepPOJO;
import org.werk.engine.processing.WerkJob;
import org.werk.meta.JobInitInfo;
import org.werk.meta.JobReviveInfo;
import org.werk.meta.OldVersionJobInitInfo;
import org.werk.processing.jobs.JobStatus;
import org.werk.processing.jobs.JoinStatusRecord;
import org.werk.processing.parameters.Parameter;
import org.werk.processing.readonly.ReadOnlyJob;
import org.werk.processing.steps.JoinResult;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.Setter;

public class LocalWerkJob<J> extends WerkJob<J> {
	@Getter
	protected J jobId;
	@Getter @Setter
	protected AtomicLong stepCount;
	@Setter
	protected List<StepPOJO> processingHistory;
	
	protected LocalJobManager<J> jobManager;  
	
	public LocalWerkJob(J jobId, String jobTypeName, long version, Optional<String> jobName, JobStatus status,
			Map<String, Parameter> jobInitialParameters, Map<String, Parameter> jobParameters,
			Timestamp nextExecutionTime, Optional<JoinStatusRecord<J>> joinStatusRecord, Optional<J> parentJobId,
			LocalJobManager<J> jobManager) {
		super(jobTypeName, version, jobName, status, jobInitialParameters, jobParameters, nextExecutionTime, 
				joinStatusRecord, parentJobId);
		this.jobId = jobId;
		stepCount = new AtomicLong(0);
		processingHistory = new ArrayList<>();
		this.parentJobId = parentJobId;
		this.jobManager = jobManager;
	}

	public long getNextStepNumber() {
		return stepCount.incrementAndGet();
	}
	
	@Override
	public List<StepPOJO> getProcessingHistory() {
		return processingHistory;
	}

	@Override
	public List<StepPOJO> getFilteredHistory(String stepTypeName) {
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

	@Override
	public String joinResultToStr(JoinResult<J> joinResult) {
		try {
			LocalJoinResult<J> ljr = (LocalJoinResult<J>)joinResult;
			
			ObjectMapper objectMapper = new ObjectMapper();
			return objectMapper.writeValueAsString(ljr);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public JoinResult<J> strToJoinResult(String joinResultStr) {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			return objectMapper.readValue(joinResultStr, new TypeReference<JoinResult<J>>() {});
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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
	
	//----
	
	@Override
	public ReadOnlyJob<J> loadJobAndHistory(J jobIds) {
		return loadJob(jobIds);
	}

	@Override
	public List<ReadOnlyJob<J>> loadJobsAndHistory(Collection<J> jobIds) {
		return loadJobs(jobIds);
	}

	@Override
	public List<ReadOnlyJob<J>> loadAllChildJobsAndHistory() {
		return loadAllChildJobs();
	}

	@Override
	public List<ReadOnlyJob<J>> loadChildJobsOfTypesAndHistory(Set<String> jobTypes) {
		return loadChildJobsOfTypes(jobTypes);
	}
}
