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

	public DefaultValueJobInputParameterImpl(String name, ParameterType type, String description,
			boolean isDefaultValueImmutable, Parameter defaultValue) {
		super(name, type, false, description);
		
		this.isDefaultValueImmutable = isDefaultValueImmutable;
		this.defaultValue = defaultValue;
	}
	
	@Override
	public String getConstraints() {
		return String.format(
				"%sDefault", isDefaultValueImmutable() ? "Immutable " : ""
			);
	}
	
	@Override
	public String toString() {
		return String.format(
				"%s %s [%sDefault: %s]", type.toString(), name, isDefaultValueImmutable() ? "Immutable " : "", 
						getParameterValue(defaultValue) 
			);
	}
}
