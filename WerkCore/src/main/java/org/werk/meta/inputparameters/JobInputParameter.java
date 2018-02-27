package org.werk.meta.inputparameters;

import org.werk.processing.parameters.ParameterType;

public interface JobInputParameter {
	String getName();
	ParameterType getType();
	String getDescription();
	boolean isOptional();
	
	String getConstraints();
}
