package org.werk.engine.processing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.werk.processing.jobs.JobToken;
import org.werk.processing.parameters.Parameter;

public class JobContext extends ParameterContext {
	protected List<JobToken> createdJobs;

	public JobContext(Map<String, Parameter> stepParameters) {
		this(stepParameters, new ArrayList<>());
	}
	
	public JobContext(Map<String, Parameter> stepParameters, List<JobToken> createdJobs) {
		super(stepParameters);
		this.createdJobs = createdJobs;
	}
	
	public JobContext cloneContext() {
		Map<String, Parameter> stepParameters0 = new HashMap<>();
		for (Map.Entry<String, Parameter> stepParameter : parameters.entrySet())
			stepParameters0.put(stepParameter.getKey(), cloneParameter(stepParameter.getValue()));
		List<JobToken> createdJobs = new ArrayList<>(this.createdJobs);
		
		return new JobContext(stepParameters0, createdJobs);
	}
	
	public List<JobToken> getCreatedJobs() {
		return Collections.unmodifiableList(createdJobs);
	}
	
	public void addCreatedJob(JobToken jobToken) {
		createdJobs.add(jobToken);
	}
}
