package org.werk.processing.parameters.impl;

import org.werk.processing.parameters.DoubleParameter;
import org.werk.processing.parameters.ParameterType;

import lombok.Getter;

public class DoubleParameterImpl implements DoubleParameter {
	@Getter
	protected double value;
	
	public DoubleParameterImpl(double value) {
		this.value = value;
	}
	
	@Override
	public ParameterType getType() {
		return ParameterType.DOUBLE;
	}
}
