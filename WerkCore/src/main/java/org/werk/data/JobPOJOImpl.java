package org.werk.data;

import java.util.Map;
import java.util.Optional;

import org.pillar.time.interfaces.Timestamp;
import org.werk.data.JobPOJO;
import org.werk.processing.jobs.JobStatus;
import org.werk.processing.jobs.JoinStatusRecord;
import org.werk.processing.parameters.Parameter;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class JobPOJOImpl<J> implements JobPOJO<J> {
	@Getter
	protected String jobTypeName;
	@Getter
	protected long version;
	@Getter
	protected J jobId;
	@Getter
	protected Optional<String> jobName;
	@Getter
	protected Optional<J> parentJobId;
	@Getter
	protected int stepCount;
	@Getter
	protected String currentStepTypeName;
	@Getter
	protected Map<String, Parameter> jobInitialParameters;
	@Getter
	protected JobStatus status;
	@Getter
	protected Timestamp creationTime;
	@Getter
	protected Timestamp nextExecutionTime;
	@Getter
	protected Map<String, Parameter> jobParameters;
	@Getter
	protected Optional<JoinStatusRecord<J>> joinStatusRecord;
}
