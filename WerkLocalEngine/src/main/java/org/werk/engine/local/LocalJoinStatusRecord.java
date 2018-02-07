package org.werk.engine.local;

import java.util.Collection;
import java.util.Set;

import org.werk.processing.jobs.JobStatus;
import org.werk.processing.jobs.JoinStatusRecord;
import org.werk.processing.readonly.ReadOnlyJob;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class LocalJoinStatusRecord<J> implements JoinStatusRecord<J> {
	Set<J> joinedJobs;
	@Getter
	String joinParameterName;
	@Getter
	JobStatus statusBeforeJoin;
	@Getter
	int waitForNJobs;
	@Getter
	LocalJobManager<J> jobManager;
	
	@Override
	public Collection<J> getJoinedJobIds() {
		return joinedJobs;
	}
	@Override
	public JobStatus getJoinedJobStatus(J joinedJobId) {
		if (!joinedJobs.contains(joinedJobId))
			return null;
		ReadOnlyJob<J> job = jobManager.getJob(joinedJobId);
		return (job == null) ? JobStatus.UNDEFINED : job.getStatus();
	}
}
