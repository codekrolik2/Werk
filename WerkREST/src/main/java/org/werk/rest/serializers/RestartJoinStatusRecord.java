package org.werk.rest.serializers;

import java.util.Collection;

import org.werk.processing.jobs.JobStatus;
import org.werk.processing.jobs.JoinStatusRecord;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class RestartJoinStatusRecord<J> implements JoinStatusRecord<J> {
	@Getter
	Collection<J> joinedJobIds;

	public JobStatus getJoinedJobStatus(J joinedJobId) {
		return JobStatus.UNDEFINED;
	}

	@Getter
	String joinParameterName;

	@Getter
	int waitForNJobs;
}
