package org.werk.engine.sql.DAO;

import java.util.Map;
import java.util.Optional;

import org.pillar.time.interfaces.Timestamp;
import org.werk.data.JobPOJOImpl;
import org.werk.processing.jobs.JobStatus;
import org.werk.processing.jobs.JoinStatusRecord;
import org.werk.processing.parameters.Parameter;

import lombok.Getter;
import lombok.Setter;

public class DBJobPOJO extends JobPOJOImpl<Long> {
	@Getter @Setter
	protected long currentStepId;
	@Getter
	protected Optional<Long> idLocker;
	
	public DBJobPOJO(String jobTypeName, long version, Long jobId, Optional<String> jobName, Optional<Long> parentJobId,
			int stepCount, String currentStepTypeName, Map<String, Parameter> jobInitialParameters, JobStatus status,
			Timestamp creationTime, Timestamp nextExecutionTime, Map<String, Parameter> jobParameters,
			Optional<JoinStatusRecord<Long>> joinStatusRecord, long currentStepId, Optional<Long> idLocker) {
		super(jobTypeName, version, jobId, jobName, parentJobId, stepCount, currentStepTypeName, jobInitialParameters, status,
				creationTime, nextExecutionTime, jobParameters, joinStatusRecord);
		
		this.currentStepId = currentStepId;
		this.idLocker = idLocker;
	}
	
	public void setJoinStatusRecord(Optional<JoinStatusRecord<Long>> joinStatusRecord) {
		this.joinStatusRecord = joinStatusRecord;
	}
}
