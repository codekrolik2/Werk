package org.werk.parameters;

import org.werk.parameters.interfaces.BoolParameter;
import org.werk.parameters.interfaces.ParameterType;

import lombok.Getter;

public class BoolParameterImpl implements BoolParameter {
	@Getter
	protected boolean value;
	
	public BoolParameterImpl(boolean value) {
		this.value = value;
	}
	
	@Override
	public ParameterType getType() {
		return ParameterType.BOOL;
	}
}
