package org.werk.meta.impl;

import java.util.Map;
import java.util.Optional;

import org.werk.meta.JobInitInfo;
import org.werk.processing.parameters.Parameter;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
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
}
