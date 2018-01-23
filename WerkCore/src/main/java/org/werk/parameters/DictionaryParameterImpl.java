package org.werk.parameters;

import java.util.HashMap;
import java.util.Map;

import org.werk.parameters.interfaces.DictionaryParameter;
import org.werk.parameters.interfaces.Parameter;
import org.werk.parameters.interfaces.ParameterType;

import lombok.Getter;

public class DictionaryParameterImpl implements DictionaryParameter {
	@Getter
	protected Map<String, Parameter> value;
	
	public DictionaryParameterImpl(Map<String, Parameter> value) {
		this.value = value;
	}
	
	public DictionaryParameterImpl() {
		this.value = new HashMap<>();
	}
	
	@Override
	public ParameterType getType() {
		return ParameterType.DICTIONARY;
	}
}
