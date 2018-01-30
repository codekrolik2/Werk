package org.werk.processing.jobs;

public enum JobStatus {
	INACTIVE,
	PROCESSING,
	ROLLING_BACK,
	
	JOINING,
	
	FINISHED,
	ROLLED_BACK,
	FAILED
}
