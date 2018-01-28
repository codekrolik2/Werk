package org.werk.processing.steps;

import java.util.Map;

import org.werk.processing.jobs.JobStatus;

public interface JoinResult<J> {
	Map<J, JobStatus> getJoinedJobs();
}
