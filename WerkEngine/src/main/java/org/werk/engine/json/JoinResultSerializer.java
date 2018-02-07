package org.werk.engine.json;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONObject;
import org.werk.processing.jobs.JobStatus;
import org.werk.processing.steps.JoinResult;

public class JoinResultSerializer<J> {
	protected JobIdSerializer<J> jobIdSerializer;
	
	public JoinResultSerializer(JobIdSerializer<J> jobIdSerializer) {
		this.jobIdSerializer = jobIdSerializer;
	}
	
	public JoinResult<J> deserializeJoinResult(JSONObject joinResultJSON) {
		Map<J, JobStatus> joinedJobs = new HashMap<>();
		
		Iterator<?> keys = joinResultJSON.keys();
		while (keys.hasNext()) {
		    String key = (String)keys.next();
		    String statusName = joinResultJSON.getString(key);
		    JobStatus status = JobStatus.valueOf(statusName);
		    
		    joinedJobs.put(jobIdSerializer.deSerializeJobId(key), status);
		}
		
		JoinResultImpl<J> r = new JoinResultImpl<J>(joinedJobs);
		return r;
	}
	
	public JSONObject serializeJoinResult(JoinResult<J> joinResult) {
		JSONObject jrObj = new JSONObject();
		for (J jobId : joinResult.getJoinedJobIds()) {
			JobStatus status = joinResult.getJoinedJobStatus(jobId);
			jrObj.put(jobIdSerializer.serializeJobId(jobId), status.toString());
		}
		
		return jrObj;
	}
}
