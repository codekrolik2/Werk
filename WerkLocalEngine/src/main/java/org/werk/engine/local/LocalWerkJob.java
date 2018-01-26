package org.werk.engine.local;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.pillar.time.interfaces.Timestamp;
import org.werk.engine.processing.WerkJob;
import org.werk.meta.JobInitInfo;
import org.werk.meta.OldVersionJobInitInfo;
import org.werk.processing.jobs.JobStatus;
import org.werk.processing.jobs.JobToken;
import org.werk.processing.parameters.Parameter;
import org.werk.processing.readonly.ReadOnlyJob;
import org.werk.processing.readonly.ReadOnlyStep;

public class LocalWerkJob extends WerkJob {
	AtomicLong stepCount;
	List<ReadOnlyStep> processingHistory;
	
	public LocalWerkJob(String jobTypeName, long version, Optional<String> jobName, JobStatus status,
			Map<String, Parameter> jobInitialParameters, Map<String, Parameter> jobParameters,
			Timestamp nextExecutionTime, List<JobToken> jobsToJoin) {
		super(jobTypeName, version, jobName, status, jobInitialParameters, jobParameters, nextExecutionTime, jobsToJoin);
		stepCount = new AtomicLong(0);
		processingHistory = new ArrayList<>();
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
	
	//----------------------------------------------

	@Override
	public ReadOnlyJob loadJob(JobToken token) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ReadOnlyJob> loadJobs(Collection<JobToken> token) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ReadOnlyJob> loadAllChildJobs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ReadOnlyJob> loadChildJobsOfTypes(Collection<String> jobTypes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ReadOnlyJob loadJobAndHistory(JobToken token) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ReadOnlyJob> loadJobsAndHistory(Collection<JobToken> token) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ReadOnlyJob> loadAllChildJobsAndHistory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ReadOnlyJob> loadChildJobsOfTypesAndHistory(Collection<String> jobTypes) {
		// TODO Auto-generated method stub
		return null;
	}
	
	//----------------------------------------------
	
	@Override
	public JobToken fork(JobInitInfo jobInitInfo) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JobToken forkOldVersion(OldVersionJobInitInfo jobInitInfo) {
		// TODO Auto-generated method stub
		return null;
	}
}
