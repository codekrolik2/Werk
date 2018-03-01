package org.werk.meta.impl;

import java.util.Map;
import java.util.Optional;

import org.pillar.time.interfaces.Timestamp;
import org.werk.meta.JobInitInfo;
import org.werk.processing.parameters.Parameter;

import lombok.Getter;

public class JobInitInfoImpl implements JobInitInfo {
	@Getter
	protected String jobTypeName;
	@Getter
	protected Optional<String> initSignatureName;
	@Getter
	protected Map<String, Parameter> initParameters;
	@Getter
	protected Optional<String> jobName;
	@Getter
	protected Optional<Timestamp> nextExecutionTime;
	
	public JobInitInfoImpl(String jobTypeName, Optional<String> initSignatureName,
			Map<String, Parameter> initParameters, Optional<String> jobName, Optional<Timestamp> nextExecutionTime) {
		this.jobTypeName = jobTypeName;
		this.initSignatureName = initSignatureName;
		this.initParameters = initParameters;
		this.jobName = jobName;
		this.nextExecutionTime = nextExecutionTime;
	}
}
