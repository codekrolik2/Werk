package org.werk.meta.impl;

import java.util.Map;
import java.util.Optional;

import org.werk.meta.JobInitInfo;
import org.werk.meta.OverflowAction;
import org.werk.processing.parameters.Parameter;

import lombok.Getter;

public class JobInitInfoImpl implements JobInitInfo {
	@Getter
	protected String jobTypeName;
	@Getter
	protected Optional<String> jobName;
	@Getter
	protected Map<String, Parameter> initParameters;
	@Getter
	protected Optional<Long> historyLimit;
	@Getter
	protected Optional<OverflowAction> overflowAction;
	
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

	public JobInitInfoImpl(String jobTypeName, String jobName, Map<String, Parameter> initParameters, 
			Long historyLimit, OverflowAction overflowAction) {
		this.jobTypeName = jobTypeName;
		this.jobName = Optional.of(jobName);
		this.initParameters = initParameters;
		this.historyLimit = Optional.of(historyLimit);
		this.overflowAction = Optional.of(overflowAction);
	}

	public JobInitInfoImpl(String jobTypeName, Map<String, Parameter> initParameters, 
			Long historyLimit, OverflowAction overflowAction) {
		this.jobTypeName = jobTypeName;
		this.initParameters = initParameters;
		this.historyLimit = Optional.of(historyLimit);
		this.overflowAction = Optional.of(overflowAction);
	}
}
