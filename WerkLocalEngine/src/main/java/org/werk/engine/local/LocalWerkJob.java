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
import org.werk.engine.processing.WerkJob;
import org.werk.meta.JobInitInfo;
import org.werk.meta.JobReviveInfo;
import org.werk.meta.OldVersionJobInitInfo;
import org.werk.processing.jobs.JobStatus;
import org.werk.processing.jobs.JobToken;
import org.werk.processing.jobs.JoinStatusRecord;
import org.werk.processing.parameters.Parameter;
import org.werk.processing.readonly.ReadOnlyJob;
import org.werk.processing.readonly.ReadOnlyStep;
import org.werk.processing.steps.JoinResult;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.Setter;

public class LocalWerkJob extends WerkJob {
	@Getter
	protected long jobId;
	@Getter @Setter
	protected AtomicLong stepCount;
	@Setter
	protected List<ReadOnlyStep> processingHistory;
	
	protected LocalJobManager jobManager;  
	
	public LocalWerkJob(long jobId, String jobTypeName, long version, Optional<String> jobName, JobStatus status,
			Map<String, Parameter> jobInitialParameters, Map<String, Parameter> jobParameters,
			Timestamp nextExecutionTime, Optional<JoinStatusRecord> joinStatusRecord, Optional<JobToken> parentJobToken,
			LocalJobManager jobManager) {
		super(jobTypeName, version, jobName, status, jobInitialParameters, jobParameters, nextExecutionTime, 
				joinStatusRecord, parentJobToken);
		this.jobId = jobId;
		stepCount = new AtomicLong(0);
		processingHistory = new ArrayList<>();
		this.parentJobToken = parentJobToken;
		this.jobManager = jobManager;
	}

	public long getNextStepNumber() {
		return stepCount.incrementAndGet();
	}
	
	@Override
	public List<ReadOnlyStep> getProcessingHistory() {
		return processingHistory;
	}

	@Override
	public List<ReadOnlyStep> getFilteredHistory(String stepTypeName) {
		return processingHistory.stream().
				filter(a -> a.getStepTypeName().equals(stepTypeName)).collect(Collectors.toList());
	}

	@Override
	public ReadOnlyStep getStep(long stepNumber) {
		for (ReadOnlyStep historyStep : processingHistory)
			if (historyStep.getStepNumber() == stepNumber)
				return historyStep;
		
		return null;
	}
	
	@Override
	public String tokenToStr(JobToken token) {
		return Long.toString(((LongToken)token).getValue());
	}

	@Override
	public JobToken strToToken(String token) {
		return new LongToken(Long.parseLong(token));
	}

	@Override
	public String joinResultToStr(JoinResult joinResult) {
		try {
			LocalJoinResult ljr = (LocalJoinResult)joinResult;
			
			ObjectMapper objectMapper = new ObjectMapper();
			return objectMapper.writeValueAsString(ljr);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public JoinResult strToJoinResult(String joinResultStr) {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			return objectMapper.readValue(joinResultStr, LocalJoinResult.class);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	//----------------------------------------------
	
	@Override
	public JobToken fork(JobInitInfo jobInitInfo) throws Exception {
		return jobManager.createJob(jobInitInfo, Optional.of(new LongToken(this.getJobId())));
	}

	@Override
	public JobToken forkOldVersion(OldVersionJobInitInfo jobInitInfo) throws Exception {
		return jobManager.createOldVersionJob(jobInitInfo, Optional.of(new LongToken(this.getJobId())));
	}

	@Override
	public void revive(JobReviveInfo jobReviveInfo) throws Exception {
		jobManager.reviveJob(jobReviveInfo);
	}

	//----------------------------------------------

	@Override
	public ReadOnlyJob loadJob(JobToken token) {
		return jobManager.getJob(((LongToken)token).getValue());
	}

	@Override
	public List<ReadOnlyJob> loadJobs(Collection<JobToken> tokens) {
		return jobManager.getJobs(
				tokens.stream().map(a -> ((LongToken)a).getValue()).collect(Collectors.toList())
			);
	}

	@Override
	public List<ReadOnlyJob> loadAllChildJobs() {
		return jobManager.getAllChildJobs(getJobId());
	}

	@Override
	public List<ReadOnlyJob> loadChildJobsOfTypes(Set<String> jobTypes) {
		return jobManager.getChildJobsOfTypes(getJobId(), jobTypes);
	}
	
	//----
	
	@Override
	public ReadOnlyJob loadJobAndHistory(JobToken token) {
		return loadJob(token);
	}

	@Override
	public List<ReadOnlyJob> loadJobsAndHistory(Collection<JobToken> token) {
		return loadJobs(token);
	}

	@Override
	public List<ReadOnlyJob> loadAllChildJobsAndHistory() {
		return loadAllChildJobs();
	}

	@Override
	public List<ReadOnlyJob> loadChildJobsOfTypesAndHistory(Set<String> jobTypes) {
		return loadChildJobsOfTypes(jobTypes);
	}
}
