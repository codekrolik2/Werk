package org.werk.rest.pojo;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;
import org.pillar.time.interfaces.Timestamp;
import org.werk.data.StepPOJO;
import org.werk.processing.jobs.JobStatus;
import org.werk.processing.jobs.JoinStatusRecord;
import org.werk.processing.parameters.Parameter;
import org.werk.processing.readonly.ReadOnlyJobImpl;

import lombok.Getter;

public class RESTJob<J> extends ReadOnlyJobImpl<J> {
	@Getter
	JSONObject json;
	
	public RESTJob(JSONObject json, String jobTypeName, long version, J jobId, Optional<String> jobName, Optional<J> parentJobId,
			int stepCount, String currentStepTypeName, Map<String, Parameter> jobInitialParameters, JobStatus status,
			Timestamp creationTime, Timestamp nextExecutionTime, Map<String, Parameter> jobParameters,
			Optional<JoinStatusRecord<J>> joinStatusRecord, Collection<StepPOJO> processingHistory) {
		super(jobTypeName, version, jobId, jobName, parentJobId, stepCount, currentStepTypeName, jobInitialParameters, status,
				creationTime, nextExecutionTime, jobParameters, joinStatusRecord, processingHistory);
		this.json = json;
	}
}
