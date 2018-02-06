package org.werk.processing.jobs;

import java.util.List;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class JoinStatusRecordImpl<J> implements JoinStatusRecord<J> {
	@Getter
	List<J> joinedJobs;
	@Getter
	String joinParameterName;
	@Getter
	JobStatus statusBeforeJoin;
	@Getter
	Optional<Long> waitForNJobs;
}
