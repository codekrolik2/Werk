package org.werk.rest.serializers;

import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;
import org.pillar.time.interfaces.TimeProvider;
import org.pillar.time.interfaces.Timestamp;
import org.werk.meta.JobInitInfo;
import org.werk.meta.VersionJobInitInfo;
import org.werk.meta.impl.JobInitInfoImpl;
import org.werk.meta.impl.VersionJobInitInfoImpl;
import org.werk.processing.parameters.Parameter;
import org.werk.util.ParameterContextSerializer;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class JobInitInfoSerializer {
	ParameterContextSerializer parameterSerializer;
	TimeProvider timeProvider;
	
	public JobInitInfo deserializeJobInitInfo(JSONObject jobInitInfoJSON) {
		String jobTypeName = jobInitInfoJSON.getString("jobTypeName");
		Optional<String> jobName = jobInitInfoJSON.has("jobName") ? 
				Optional.of(jobInitInfoJSON.getString("jobName")) : Optional.empty();
		Map<String, Parameter> initParameters = 
				parameterSerializer.deserializeParameters(jobInitInfoJSON.getJSONObject("initParameters"));
		Optional<Timestamp> nextExecutionTime = jobInitInfoJSON.has("nextExecutionTime") ?
				Optional.of(timeProvider.createTimestamp(jobInitInfoJSON.getString("nextExecutionTime"))) : 
				Optional.empty();
				
		return new JobInitInfoImpl(jobTypeName, jobName, initParameters, nextExecutionTime);
	}
	
	public VersionJobInitInfo deserializeVersionJobInitInfo(JSONObject versionJobInitInfoJSON) {
		String jobTypeName = versionJobInitInfoJSON.getString("jobTypeName");
		Optional<String> jobName = versionJobInitInfoJSON.has("jobName") ? 
				Optional.of(versionJobInitInfoJSON.getString("jobName")) : Optional.empty();
		Map<String, Parameter> initParameters = 
				parameterSerializer.deserializeParameters(versionJobInitInfoJSON.getJSONObject("initParameters"));
		long jobVersion = versionJobInitInfoJSON.getLong("jobVersion");
		Optional<Timestamp> nextExecutionTime = versionJobInitInfoJSON.has("nextExecutionTime") ?
				Optional.of(timeProvider.createTimestamp(versionJobInitInfoJSON.getString("nextExecutionTime"))) : 
				Optional.empty();
		
		return new VersionJobInitInfoImpl(jobTypeName, initParameters, jobVersion, jobName, nextExecutionTime);
	}
}
