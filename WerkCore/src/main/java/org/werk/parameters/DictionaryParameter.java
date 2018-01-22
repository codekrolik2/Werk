package org.werk.parameters;

import java.util.HashMap;
import java.util.Map;

import org.werk.parameters.interfaces.Parameter;
import org.werk.parameters.interfaces.ParameterType;

import lombok.Getter;

public class DictionaryParameter implements Parameter {
	@Getter
	protected Map<String, Parameter> dictionary;
	
	public DictionaryParameter(Map<String, Parameter> dictionary) {
		this.dictionary = dictionary;
	}
	
	public DictionaryParameter() {
		this.dictionary = new HashMap<>();
	}
	
	@Override
	public ParameterType getType() {
		return ParameterType.DICTIONARY;
	}
}
