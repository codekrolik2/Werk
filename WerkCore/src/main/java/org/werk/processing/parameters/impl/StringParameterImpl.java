package org.werk.processing.parameters.impl;

import org.werk.processing.parameters.ParameterType;
import org.werk.processing.parameters.StringParameter;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class StringParameterImpl implements StringParameter {
	@Getter @Setter
	protected String value;
	
	public StringParameterImpl(String value) {
		this.value = value;
	}
	
	@Override
	public ParameterType getType() {
		return ParameterType.STRING;
	}
}
