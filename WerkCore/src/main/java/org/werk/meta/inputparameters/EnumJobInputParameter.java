package org.werk.meta.inputparameters;

import java.util.List;

import org.werk.processing.parameters.Parameter;

public interface EnumJobInputParameter extends JobInputParameter {
	List<Parameter> getValues();
	boolean isProhibitValues();
}
