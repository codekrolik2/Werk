package org.werk.engine.local.service;

import java.util.Map;
import java.util.Optional;

import org.werk.meta.JobInitInfo;
import org.werk.processing.parameters.Parameter;

import lombok.Getter;

public class JobInitInfoImpl implements JobInitInfo {
	@Getter
	protected String jobTypeName;
	@Getter
	protected Optional<String> jobName;
	@Getter
	protected Map<String, Parameter> initParameters;
	
	public JobInitInfoImpl(String jobTypeName, Map<String, Parameter> initParameters) {
		this.jobTypeName = jobTypeName;
		this.jobName = Optional.empty();
		this.initParameters = initParameters;
	}
	
	public JobInitInfoImpl(String jobTypeName, String jobName, Map<String, Parameter> initParameters) {
		this.jobTypeName = jobTypeName;
		this.jobName = Optional.of(jobName);
		this.initParameters = initParameters;
	}
}
