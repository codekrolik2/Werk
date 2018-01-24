package org.werk.data;

import java.util.Map;
import java.util.Optional;

import org.pillar.time.interfaces.Timestamp;
import org.werk.processing.jobs.JobStatus;
import org.werk.processing.parameters.Parameter;

public interface JobPOJO {
	String getJobTypeName();
	
	Optional<String> getJobName();
	JobStatus getStatus();

	Map<String, Parameter> getJobInitialParameters();
	Map<String, Parameter> getJobParameters();
	
	Timestamp getNextExecutionTime();
}
