package org.werk.meta.inputparameters;

import org.werk.processing.parameters.Parameter;

public interface DefaultValueJobInputParameter extends JobInputParameter {
	boolean isDefaultValueImmutable();
	Parameter getDefaultValue();
}
