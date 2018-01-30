package org.werk.meta.inputparameters.impl;

import org.werk.meta.inputparameters.JobInputParameter;
import org.werk.processing.parameters.BoolParameter;
import org.werk.processing.parameters.DictionaryParameter;
import org.werk.processing.parameters.DoubleParameter;
import org.werk.processing.parameters.ListParameter;
import org.werk.processing.parameters.LongParameter;
import org.werk.processing.parameters.Parameter;
import org.werk.processing.parameters.ParameterType;
import org.werk.processing.parameters.StringParameter;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class JobInputParameterImpl implements JobInputParameter {
	@Getter
	protected String name;
	@Getter
	protected ParameterType type;
	@Getter
	protected boolean isOptional;
	@Getter
	protected String description;

	protected String getParameterValue(Parameter prm) {
		if (prm instanceof BoolParameter)
			return ((BoolParameter)prm).getValue().toString();
		if (prm instanceof DictionaryParameter)
			return ((DictionaryParameter)prm).getValue().toString();
		if (prm instanceof DoubleParameter)
			return ((DoubleParameter)prm).getValue().toString();
		if (prm instanceof ListParameter)
			return ((ListParameter)prm).getValue().toString();
		if (prm instanceof LongParameter)
			return ((LongParameter)prm).getValue().toString();
		if (prm instanceof StringParameter)
			return ((StringParameter)prm).getValue().toString();
		return prm.toString();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(name).append(": ").append(type.toString());
		if (isOptional())
			sb.append(" ").append("[Optional]");
		
		return sb.toString();
	}
}