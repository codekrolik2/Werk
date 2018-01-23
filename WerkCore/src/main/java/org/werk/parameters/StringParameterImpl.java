package org.werk.parameters;

import org.werk.parameters.interfaces.ParameterType;
import org.werk.parameters.interfaces.StringParameter;

import lombok.Getter;

public class StringParameterImpl implements StringParameter {
	@Getter
	protected String value;
	
	public StringParameterImpl(String value) {
		this.value = value;
	}
	
	@Override
	public ParameterType getType() {
		return ParameterType.STRING;
	}
}
