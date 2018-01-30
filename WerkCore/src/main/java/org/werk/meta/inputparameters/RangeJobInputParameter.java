package org.werk.meta.inputparameters;

import org.werk.processing.parameters.Parameter;

public interface RangeJobInputParameter extends JobInputParameter {
	Parameter getStart();
	boolean isStartInclusive();
	Parameter getEnd();
	boolean isEndInclusive();
	
	boolean isProhibitRange();
}
