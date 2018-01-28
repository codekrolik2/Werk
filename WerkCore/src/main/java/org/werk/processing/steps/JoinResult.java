package org.werk.processing.steps;

import java.util.Map;

import org.werk.processing.jobs.JobStatus;
import org.werk.processing.jobs.JobToken;

public interface JoinResult {
	Map<JobToken, JobStatus> getJoinedJobs();
}
