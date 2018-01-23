package org.werk.parameters;

import java.util.ArrayList;
import java.util.List;

import org.werk.parameters.interfaces.ListParameter;
import org.werk.parameters.interfaces.Parameter;
import org.werk.parameters.interfaces.ParameterType;

import lombok.Getter;

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
