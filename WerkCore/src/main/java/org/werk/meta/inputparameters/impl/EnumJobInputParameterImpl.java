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

	@Override
	public String getConstraints() {
		StringBuilder sb = new StringBuilder();
		if (isOptional())
			sb.append("Optional ");

		if (prohibitValues)
			sb.append("Prohibit Values: ");
		else
			sb.append("Enum: ");
		
		sb.append("[");
		for (int i = 0; i < values.size(); i++) {
			Parameter prm = values.get(i);
			if (i > 0) sb.append(", ");
			sb.append(getParameterValue(prm));
		}
		
		sb.append("]");
		
		return sb.toString();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (isOptional())
			sb.append("Optional ");
		sb.append(type.toString()).append(" ").append(name);
		sb.append(" [");
		if (prohibitValues)
			sb.append("Prohibit Values: ");
		else
			sb.append("Enum: ");
		
		for (int i = 0; i < values.size(); i++) {
			Parameter prm = values.get(i);
			if (i > 0) sb.append(", ");
			sb.append(getParameterValue(prm));
		}
		
		sb.append("]");
		
		return sb.toString();
	}
}
