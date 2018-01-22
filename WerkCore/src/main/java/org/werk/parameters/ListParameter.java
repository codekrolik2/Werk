package org.werk.parameters;

import java.util.ArrayList;
import java.util.List;

import org.werk.parameters.interfaces.Parameter;
import org.werk.parameters.interfaces.ParameterType;

import lombok.Getter;

public class ListParameter implements Parameter {
	@Getter
	protected List<Parameter> list;
	
	public ListParameter(List<Parameter> list) {
		this.list = list;
	}
	
	public ListParameter() {
		this.list = new ArrayList<Parameter>();
	}
	
	@Override
	public ParameterType getType() {
		return ParameterType.LIST;
	}
}
