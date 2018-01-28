package org.werk.engine.local;

import java.util.List;

import org.werk.processing.jobs.JobStatus;
import org.werk.processing.jobs.JobToken;
import org.werk.processing.jobs.JoinStatusRecord;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class LocalJoinStatusRecord implements JoinStatusRecord {
	@Getter
	List<JobToken> joinedJobs;
	@Getter
	String joinParameterName;
	@Getter
	JobStatus statusBeforeJoin;
}
