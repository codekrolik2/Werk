package com.github.bamirov.werk.parameters;

import java.util.ArrayList;
import java.util.List;

import com.github.bamirov.werk.parameters.interfaces.Parameter;
import com.github.bamirov.werk.parameters.interfaces.ParameterType;

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
