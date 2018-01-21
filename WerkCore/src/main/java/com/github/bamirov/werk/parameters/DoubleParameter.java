package com.github.bamirov.werk.parameters;

import com.github.bamirov.werk.parameters.interfaces.Parameter;
import com.github.bamirov.werk.parameters.interfaces.ParameterType;

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
