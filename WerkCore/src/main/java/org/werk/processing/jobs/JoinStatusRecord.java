package org.werk.processing.jobs;

import java.util.List;

public interface JoinStatusRecord<J> {
	List<J> getJoinedJobs();
	String getJoinParameterName();
	JobStatus getStatusBeforeJoin();
}
