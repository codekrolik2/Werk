package org.werk.engine.json;

import java.util.Map;

import org.werk.processing.jobs.JobStatus;
import org.werk.processing.steps.JoinResult;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class JoinResultImpl<J> implements JoinResult<J> {
	@Getter @Setter
	Map<J, JobStatus> joinedJobs;
}
