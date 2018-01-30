package org.werk.meta.inputparameters.impl;

import java.util.List;

import org.werk.meta.inputparameters.EnumJobInputParameter;
import org.werk.processing.parameters.Parameter;
import org.werk.processing.parameters.ParameterType;

import lombok.Getter;

public class EnumJobInputParameterImpl extends JobInputParameterImpl implements EnumJobInputParameter {
	@Getter
	List<Parameter> values;
	@Getter
	boolean prohibitValues;
	
	public EnumJobInputParameterImpl(String name, ParameterType type, boolean isOptional, String description,
			List<Parameter> values, boolean prohibitValues) {
		super(name, type, isOptional, description);
		this.values = values;
		this.prohibitValues = prohibitValues;
	}
}
