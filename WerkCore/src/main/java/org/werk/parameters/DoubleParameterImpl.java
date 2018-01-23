package org.werk.parameters;

import org.werk.parameters.interfaces.DoubleParameter;
import org.werk.parameters.interfaces.ParameterType;

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
