package org.werk.parameters;

import org.werk.parameters.interfaces.LongParameter;
import org.werk.parameters.interfaces.ParameterType;

import lombok.Getter;

public class LongParameterImpl implements LongParameter {
	@Getter
	protected long value;
	
	public LongParameterImpl(long value) {
		this.value = value;
	}
	
	@Override
	public ParameterType getType() {
		return ParameterType.LONG;
	}
}
