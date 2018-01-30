package org.werk.processing.parameters.impl;

import java.util.ArrayList;
import java.util.List;

import org.werk.processing.parameters.ListParameter;
import org.werk.processing.parameters.Parameter;
import org.werk.processing.parameters.ParameterType;

import lombok.Getter;
import lombok.ToString;

@ToString
public class ListParameterImpl implements ListParameter {
	@Getter
	protected List<Parameter> value;
	
	public ListParameterImpl(List<Parameter> value) {
		this.value = value;
	}
	
	public ListParameterImpl() {
		this.value = new ArrayList<Parameter>();
	}
	
	@Override
	public ParameterType getType() {
		return ParameterType.LIST;
	}
}
