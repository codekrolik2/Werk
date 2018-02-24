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
		return String.format(
				"%s %s [%sDefault: %s]", type.toString(), name, isDefaultValueImmutable() ? "Immutable " : "", 
						getParameterValue(defaultValue) 
			);
	}
}
