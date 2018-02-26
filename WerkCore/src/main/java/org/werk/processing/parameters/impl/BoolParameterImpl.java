package org.werk.processing.parameters.impl;

import org.werk.processing.parameters.BoolParameter;
import org.werk.processing.parameters.ParameterType;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class BoolParameterImpl implements BoolParameter {
	@Getter @Setter
	protected Boolean value;
	
	public BoolParameterImpl(Boolean value) {
		this.value = value;
	}
	
	@Override
	public ParameterType getType() {
		return ParameterType.BOOL;
	}
}
