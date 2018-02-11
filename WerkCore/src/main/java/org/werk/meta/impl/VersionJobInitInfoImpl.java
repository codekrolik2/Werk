package org.werk.meta.impl;

import java.util.Map;
import java.util.Optional;

import org.werk.meta.VersionJobInitInfo;
import org.werk.processing.parameters.Parameter;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class VersionJobInitInfoImpl implements VersionJobInitInfo {
	@Getter
	String jobTypeName;
	@Getter
	Map<String, Parameter> initParameters;
	@Getter
	long jobVersion;
	@Getter
	Optional<String> jobName;
	
	public VersionJobInitInfoImpl(String jobTypeName, Map<String, Parameter> initParameters, long jobVersion) {
		this.jobTypeName = jobTypeName;
		this.initParameters = initParameters;
		this.jobVersion = jobVersion;
		jobName = Optional.empty();
	}
}
