package com.github.bamirov.werk.parameters;

import com.github.bamirov.werk.parameters.interfaces.Parameter;
import com.github.bamirov.werk.parameters.interfaces.ParameterType;

import lombok.Getter;

public class LongParameter implements Parameter {
	@Getter
	protected long value;
	
	public LongParameter(long value) {
		this.value = value;
	}
	
	@Override
	public ParameterType getType() {
		return ParameterType.LONG;
	}
}
