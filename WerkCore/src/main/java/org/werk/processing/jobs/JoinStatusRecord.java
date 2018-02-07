package org.werk.processing.jobs;

import org.werk.processing.steps.JoinResult;

public interface JoinStatusRecord<J> extends JoinResult<J> {
	String getJoinParameterName();
	JobStatus getStatusBeforeJoin();
	int getWaitForNJobs();
}
