package org.werk.engine.sql.DAO;

import java.util.Collection;
import java.util.Map;

import org.werk.processing.jobs.JobStatus;
import org.werk.processing.jobs.JoinStatusRecord;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class SQLJoinStatusRecord<J> implements JoinStatusRecord<J> {
	@Getter
	Map<J, JobStatus> joinedJobs;
	@Getter
	String joinParameterName;
	@Getter
	int waitForNJobs;
	
	@Override
	public Collection<J> getJoinedJobIds() {
		return joinedJobs.keySet();
	}
	
	@Override
	public JobStatus getJoinedJobStatus(J joinedJobId) {
		return joinedJobs.get(joinedJobId);
	}
}
