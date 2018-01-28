package org.werk.engine.local;

import java.util.Map;

import org.werk.processing.jobs.JobStatus;
import org.werk.processing.steps.JoinResult;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class LocalJoinResult<J> implements JoinResult<J> {
	@Getter @Setter
	Map<J, JobStatus> joinedJobs;
}
