package org.werk.parameters;

import org.werk.parameters.interfaces.Parameter;
import org.werk.parameters.interfaces.ParameterType;

import lombok.Getter;

public class StringParameter implements Parameter {
	@Getter
	protected String value;
	
	public StringParameter(String value) {
		this.value = value;
	}
	
	@Override
	public ParameterType getType() {
		return ParameterType.STRING;
	}
}
