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

public class JobInitInfoSerializer {
	ParameterContextSerializer parameterSerializer;
	TimeProvider timeProvider;
	
	public JobInitInfoSerializer(ParameterContextSerializer parameterSerializer, TimeProvider timeProvider) {
		this.parameterSerializer = parameterSerializer;
		this.timeProvider = timeProvider;
	}
	
	public JobInitInfo deserializeJobInitInfo(JSONObject jobInitInfoJSON) {
		String jobTypeName = jobInitInfoJSON.getString("jobTypeName");
		
		Optional<String> initSignatureName = Optional.empty();
		if (jobInitInfoJSON.has("initSignatureName"))
			initSignatureName = Optional.of(jobInitInfoJSON.getString("initSignatureName"));
		Map<String, Parameter> initParameters = null;
		if (jobInitInfoJSON.has("initParameters")) {
			initParameters = parameterSerializer.deserializeParameters(jobInitInfoJSON.getJSONObject("initParameters"));
		
		Optional<String> jobName = jobInitInfoJSON.has("jobName") ? 
				Optional.of(jobInitInfoJSON.getString("jobName")) : Optional.empty();
		Optional<Timestamp> nextExecutionTime = jobInitInfoJSON.has("nextExecutionTime") ?
				Optional.of(timeProvider.createTimestamp(jobInitInfoJSON.getString("nextExecutionTime"))) : 
				Optional.empty();
		
		return new JobInitInfoImpl(jobTypeName, initSignatureName, initParameters, jobName, nextExecutionTime);
	}
	
	public VersionJobInitInfo deserializeVersionJobInitInfo(JSONObject versionJobInitInfoJSON) {
		String jobTypeName = versionJobInitInfoJSON.getString("jobTypeName");
		long jobVersion = versionJobInitInfoJSON.getLong("jobVersion");
		
		Optional<String> initSignatureName = Optional.empty();
		if (versionJobInitInfoJSON.has("initSignatureName"))
			initSignatureName = Optional.of(versionJobInitInfoJSON.getString("initSignatureName"));
		Map<String, Parameter> initParameters = null;
		if (versionJobInitInfoJSON.has("initParameters"))
			initParameters = parameterSerializer.deserializeParameters(versionJobInitInfoJSON.getJSONObject("initParameters"));
		
		Optional<String> jobName = versionJobInitInfoJSON.has("jobName") ? 
				Optional.of(versionJobInitInfoJSON.getString("jobName")) : Optional.empty();
		Optional<Timestamp> nextExecutionTime = versionJobInitInfoJSON.has("nextExecutionTime") ?
				Optional.of(timeProvider.createTimestamp(versionJobInitInfoJSON.getString("nextExecutionTime"))) : 
				Optional.empty();
		
		return new VersionJobInitInfoImpl(jobTypeName, initSignatureName, initParameters, jobVersion, jobName, nextExecutionTime);
	}
	
	public JSONObject serializeJobInitInfo(JobInitInfo jobInitInfo) {
		JSONObject jobInitInfoJSON = new JSONObject();
		
		jobInitInfoJSON.put("jobTypeName", jobInitInfo.getJobTypeName());
		
		if (jobInitInfo.getInitSignatureName().isPresent()) {
			jobInitInfoJSON.put("initSignatureName", jobInitInfo.getInitSignatureName().get());
			jobInitInfoJSON.put("initParameters", parameterSerializer.serializeParameters(jobInitInfo.getInitParameters()));
		}
		
		if (jobInitInfo.getJobName().isPresent())
			jobInitInfoJSON.put("jobName", jobInitInfo.getJobName().get());
		if (jobInitInfo.getNextExecutionTime().isPresent())
			jobInitInfoJSON.put("nextExecutionTime", jobInitInfo.getNextExecutionTime().get().getRawTime());
		
		return jobInitInfoJSON;
	}
	
	public JSONObject serializeVersionJobInitInfo(VersionJobInitInfo versionJobInitInfo) {
		JSONObject jobInitInfoJSON = new JSONObject();
		
		jobInitInfoJSON.put("jobTypeName", versionJobInitInfo.getJobTypeName());
		jobInitInfoJSON.put("jobVersion", versionJobInitInfo.getJobVersion());

		if (versionJobInitInfo.getInitSignatureName().isPresent()) {
			jobInitInfoJSON.put("initSignatureName", versionJobInitInfo.getInitSignatureName().get());
			jobInitInfoJSON.put("initParameters", parameterSerializer.serializeParameters(versionJobInitInfo.getInitParameters()));
		}
		
		if (versionJobInitInfo.getJobName().isPresent())
			jobInitInfoJSON.put("jobName", versionJobInitInfo.getJobName().get());
		if (versionJobInitInfo.getNextExecutionTime().isPresent())
			jobInitInfoJSON.put("nextExecutionTime", versionJobInitInfo.getNextExecutionTime().get().getRawTime());
		
		return jobInitInfoJSON;
	}
}
