package org.werk.processing.readonly;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.pillar.time.interfaces.Timestamp;
import org.werk.data.JobPOJOImpl;
import org.werk.data.StepPOJO;
import org.werk.processing.jobs.JobStatus;
import org.werk.processing.jobs.JoinStatusRecord;
import org.werk.processing.parameters.Parameter;

public class ReadOnlyJobImpl<J> extends JobPOJOImpl<J> implements ReadOnlyJob<J> {
	Map<Integer, StepPOJO> processingHistory;
	
	public ReadOnlyJobImpl(String jobTypeName, long version, J jobId, Optional<String> jobName, Optional<J> parentJobId,
			int stepCount, String currentStepTypeName, Map<String, Parameter> jobInitialParameters, JobStatus status,
			Timestamp creationTime, Timestamp nextExecutionTime, Map<String, Parameter> jobParameters,
			Optional<JoinStatusRecord<J>> joinStatusRecord, Collection<StepPOJO> processingHistory) {
		super(jobTypeName, version, jobId, jobName, parentJobId, stepCount, currentStepTypeName, jobInitialParameters, status,
				creationTime, nextExecutionTime, jobParameters, joinStatusRecord);
		this.processingHistory = processingHistory.stream().
				collect(Collectors.toMap(a -> a.getStepNumber(), a -> a));
	}

	@Override
	public Collection<StepPOJO> getProcessingHistory() throws Exception {
		return processingHistory.values();
	}

	@Override
	public Collection<StepPOJO> getFilteredHistory(String stepType) throws Exception {
		return processingHistory.values().
				stream().
				filter(a -> a.getStepTypeName().equals(stepType)).
				collect(Collectors.toList());
	}

	@Override
	public StepPOJO getStep(int stepNumber) throws Exception {
		return processingHistory.get(stepNumber);
	}
}
