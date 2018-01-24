package org.werk.meta.inputparameters;

import org.werk.processing.parameters.ParameterType;

public interface JobInputParameter {
	String getName();
	ParameterType getType();
	boolean isOptional();
	String getDescription();
}
