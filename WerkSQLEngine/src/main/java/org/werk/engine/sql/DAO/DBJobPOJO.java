package org.werk.engine.sql.DAO;

import java.util.Map;
import java.util.Optional;

import org.pillar.time.interfaces.Timestamp;
import org.werk.data.JobPOJO;
import org.werk.processing.jobs.JobStatus;
import org.werk.processing.jobs.JoinStatusRecord;
import org.werk.processing.parameters.Parameter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class DBJobPOJO implements JobPOJO<Long>{
	@Getter
	protected String jobTypeName;
	@Getter
	protected long version;
	@Getter
	protected Long jobId;
	@Getter
	protected Optional<String> jobName;
	@Getter
	protected Optional<Long> parentJobId;
	@Getter
	protected int stepCount;
	@Getter @Setter
	protected long currentStepId;
	@Getter
	protected Map<String, Parameter> jobInitialParameters;
	@Getter @Setter
	protected JobStatus status;
	@Getter
	protected Timestamp nextExecutionTime;
	@Getter
	protected Map<String, Parameter> jobParameters;
	@Getter @Setter
	protected Optional<JoinStatusRecord<Long>> joinStatusRecord;
	@Getter
	protected Optional<Long> idLocker;
}
