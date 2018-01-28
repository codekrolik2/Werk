package org.werk.data;

import java.util.Map;
import java.util.Optional;

import org.pillar.time.interfaces.Timestamp;
import org.werk.processing.jobs.JobStatus;
import org.werk.processing.jobs.JoinStatusRecord;
import org.werk.processing.parameters.Parameter;

public interface JobPOJO<J> {
	J getJobId();
	
	Optional<J> getParentJobId();
	String getJobTypeName();
	long getVersion();
	
	Optional<String> getJobName();
	JobStatus getStatus();
	
	Map<String, Parameter> getJobInitialParameters();
	Map<String, Parameter> getJobParameters();
	
	Timestamp getNextExecutionTime();
	
	Optional<JoinStatusRecord<J>> getJoinStatusRecord();
}
