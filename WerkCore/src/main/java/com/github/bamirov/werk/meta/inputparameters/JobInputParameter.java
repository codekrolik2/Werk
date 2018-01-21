package com.github.bamirov.werk.meta.inputparameters;

import com.github.bamirov.werk.parameters.interfaces.ParameterType;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class JobInputParameter {
	@Getter
	protected String name;
	@Getter
	protected ParameterType type;
	@Getter
	protected boolean isOptional;
	@Getter
	protected String description;
}
