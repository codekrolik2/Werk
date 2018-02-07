package org.werk.processing.steps;

import java.util.Collection;

import org.werk.processing.jobs.JobStatus;

public interface JoinResult<J> {
	Collection<J> getJoinedJobIds();
	JobStatus getJoinedJobStatus(J joinedJobId);
}
