package org.werk.processing.parameters.impl;

import org.werk.processing.parameters.LongParameter;
import org.werk.processing.parameters.ParameterType;

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
