package org.werk.parameters;

import org.werk.parameters.interfaces.Parameter;
import org.werk.parameters.interfaces.ParameterType;

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
