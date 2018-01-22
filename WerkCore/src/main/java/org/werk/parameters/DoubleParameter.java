package org.werk.parameters;

import org.werk.parameters.interfaces.Parameter;
import org.werk.parameters.interfaces.ParameterType;

import lombok.Getter;

public class DoubleParameter implements Parameter {
	@Getter
	protected double value;
	
	public DoubleParameter(double value) {
		this.value = value;
	}
	
	@Override
	public ParameterType getType() {
		return ParameterType.DOUBLE;
	}
}
