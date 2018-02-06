package org.werk.data;

import java.util.Map;
import java.util.Optional;

import org.pillar.time.interfaces.Timestamp;
import org.werk.processing.jobs.JobStatus;
import org.werk.processing.jobs.JoinStatusRecord;
import org.werk.processing.parameters.Parameter;

public interface JobPOJO<J> {
	String getJobTypeName();
	long getVersion();
	
	J getJobId();
	Optional<String> getJobName();
	Optional<J> getParentJobId();
	int getStepCount();
	Map<String, Parameter> getJobInitialParameters();
	
	JobStatus getStatus();
	Timestamp getNextExecutionTime();
	Map<String, Parameter> getJobParameters();
	
	Optional<JoinStatusRecord<J>> getJoinStatusRecord();
}
