package org.werk.processing.jobs;

public enum JobStatus {
	UNDEFINED,
	
	PROCESSING,
	ROLLING_BACK,
	
	JOINING,
	
	FINISHED,
	ROLLED_BACK,
	FAILED
}
