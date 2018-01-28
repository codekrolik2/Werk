package org.werk.processing.jobs;

import java.util.List;

public interface JoinStatusRecord {
	List<JobToken> getJoinedJobs();
	String getJoinParameterName();
	JobStatus getStatusBeforeJoin();
}
