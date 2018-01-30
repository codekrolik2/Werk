package org.werk.processing.jobs;

import java.util.List;
import java.util.Optional;

public interface JoinStatusRecord<J> {
	List<J> getJoinedJobs();
	String getJoinParameterName();
	JobStatus getStatusBeforeJoin();
	Optional<Long> getWaitForNJobs();
}
