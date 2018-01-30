package org.werk.engine.local;

import java.util.List;
import java.util.Optional;

import org.werk.processing.jobs.JobStatus;
import org.werk.processing.jobs.JoinStatusRecord;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class LocalJoinStatusRecord<J> implements JoinStatusRecord<J> {
	@Getter
	List<J> joinedJobs;
	@Getter
	String joinParameterName;
	@Getter
	JobStatus statusBeforeJoin;
	@Getter
	Optional<Long> waitForNJobs;
}
