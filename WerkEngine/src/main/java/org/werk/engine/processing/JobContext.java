package org.werk.engine.processing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.werk.processing.parameters.Parameter;

public class JobContext<J> extends ParameterContext {
	protected List<J> createdJobs;

	public JobContext(Map<String, Parameter> stepParameters) {
		this(stepParameters, new ArrayList<>());
	}
	
	public JobContext(Map<String, Parameter> stepParameters, List<J> createdJobs) {
		super(stepParameters);
		this.createdJobs = createdJobs;
	}
	
	public JobContext<J> cloneContext() {
		Map<String, Parameter> stepParameters0 = new HashMap<>();
		for (Map.Entry<String, Parameter> stepParameter : parameters.entrySet())
			stepParameters0.put(stepParameter.getKey(), cloneParameter(stepParameter.getValue()));
		List<J> createdJobs = new ArrayList<>(this.createdJobs);
		
		return new JobContext<J>(stepParameters0, createdJobs);
	}
	
	public List<J> getCreatedJobs() {
		return Collections.unmodifiableList(createdJobs);
	}
	
	public void addCreatedJob(J jobId) {
		createdJobs.add(jobId);
	}
}
