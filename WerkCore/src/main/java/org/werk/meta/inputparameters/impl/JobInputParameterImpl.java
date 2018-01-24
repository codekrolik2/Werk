package org.werk.meta.inputparameters.impl;

import org.werk.meta.inputparameters.JobInputParameter;
import org.werk.processing.parameters.ParameterType;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class JobInputParameterImpl implements JobInputParameter {
	@Getter
	protected String name;
	@Getter
	protected ParameterType type;
	@Getter
	protected boolean isOptional;
	@Getter
	protected String description;
}
