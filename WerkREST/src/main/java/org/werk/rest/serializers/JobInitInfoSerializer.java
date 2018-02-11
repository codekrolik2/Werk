package org.werk.rest.serializers;

import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;
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
	
	public JobInitInfo deserializeJobInitInfo(JSONObject jobInitInfoJSON) {
		String jobTypeName = jobInitInfoJSON.getString("jobTypeName");
		Optional<String> jobName = jobInitInfoJSON.has("jobName") ? 
				Optional.of(jobInitInfoJSON.getString("jobName")) : Optional.empty();
		Map<String, Parameter> initParameters = 
				parameterSerializer.deserializeParameters(jobInitInfoJSON.getJSONObject("initParameters"));
		
		return new JobInitInfoImpl(jobTypeName, jobName, initParameters);
	}
	
	public VersionJobInitInfo deserializeVersionJobInitInfo(JSONObject versionJobInitInfoJSON) {
		String jobTypeName = versionJobInitInfoJSON.getString("jobTypeName");
		Optional<String> jobName = versionJobInitInfoJSON.has("jobName") ? 
				Optional.of(versionJobInitInfoJSON.getString("jobName")) : Optional.empty();
		Map<String, Parameter> initParameters = 
				parameterSerializer.deserializeParameters(versionJobInitInfoJSON.getJSONObject("initParameters"));
		long jobVersion = versionJobInitInfoJSON.getLong("jobVersion");
		
		return new VersionJobInitInfoImpl(jobTypeName, initParameters, jobVersion, jobName);
	}
}
