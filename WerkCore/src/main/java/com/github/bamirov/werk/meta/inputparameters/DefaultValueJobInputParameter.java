package com.github.bamirov.werk.meta.inputparameters;

import com.github.bamirov.werk.parameters.interfaces.Parameter;
import com.github.bamirov.werk.parameters.interfaces.ParameterType;

import lombok.Getter;

public abstract class DefaultValueJobInputParameter extends JobInputParameter {
	@Getter
	protected boolean isDefaultValueImmutable;
	@Getter
	protected Parameter defaultValue;

	public DefaultValueJobInputParameter(String name, ParameterType type, boolean isOptional, String description,
			boolean isDefaultValueImmutable, Parameter defaultValue) {
		super(name, type, isOptional, description);
		
		this.isDefaultValueImmutable = isDefaultValueImmutable;
		this.defaultValue = defaultValue;
	}
}
