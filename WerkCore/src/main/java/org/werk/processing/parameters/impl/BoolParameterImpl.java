package org.werk.processing.parameters.impl;

import org.werk.processing.parameters.BoolParameter;
import org.werk.processing.parameters.ParameterType;

import lombok.Getter;

public class BoolParameterImpl implements BoolParameter {
	@Getter
	protected Boolean value;
	
	public BoolParameterImpl(boolean value) {
		this.value = value;
	}
	
	@Override
	public ParameterType getType() {
		return ParameterType.BOOL;
	}
}
