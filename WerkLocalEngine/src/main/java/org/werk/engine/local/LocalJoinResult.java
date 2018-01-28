package org.werk.engine.local;

import java.util.Map;

import org.werk.processing.jobs.JobStatus;
import org.werk.processing.jobs.JobToken;
import org.werk.processing.steps.JoinResult;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class LocalJoinResult implements JoinResult {
	@Getter @Setter
	Map<JobToken, JobStatus> joinedJobs;
}
