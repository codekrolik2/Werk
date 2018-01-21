package com.github.bamirov.werk.parameters;

import com.github.bamirov.werk.parameters.interfaces.Parameter;
import com.github.bamirov.werk.parameters.interfaces.ParameterType;

import lombok.Getter;

public class BoolParameter implements Parameter {
	@Getter
	protected boolean value;
	
	public BoolParameter(boolean value) {
		this.value = value;
	}
	
	@Override
	public ParameterType getType() {
		return ParameterType.BOOL;
	}
}
