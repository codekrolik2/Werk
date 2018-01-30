package org.werk.meta.inputparameters.impl;

import org.werk.meta.inputparameters.DefaultValueJobInputParameter;
import org.werk.processing.parameters.Parameter;
import org.werk.processing.parameters.ParameterType;

import lombok.Getter;

public class DefaultValueJobInputParameterImpl extends JobInputParameterImpl implements DefaultValueJobInputParameter {
	@Getter
	protected boolean isDefaultValueImmutable;
	@Getter
	protected Parameter defaultValue;

	public DefaultValueJobInputParameterImpl(String name, ParameterType type, boolean isOptional, String description,
			boolean isDefaultValueImmutable, Parameter defaultValue) {
		super(name, type, isOptional, description);
		
		this.isDefaultValueImmutable = isDefaultValueImmutable;
		this.defaultValue = defaultValue;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(name).append(": ").append(type.toString());
		sb.append(" [").append("Default: ").append(getParameterValue(defaultValue));
		if (isDefaultValueImmutable())
			sb.append(", Immutable]");
		else
			sb.append(", Mutable]");
		
		return sb.toString();
	}
}
