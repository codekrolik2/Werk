package org.werk.ui.controls.parameters.state;

import org.werk.meta.inputparameters.JobInputParameter;
import org.werk.processing.parameters.Parameter;
import org.werk.processing.parameters.ParameterType;

import lombok.Getter;
import lombok.Setter;

public class PrimitiveParameterInit extends ParameterInit {
	public PrimitiveParameterInit(JobInputParameter jobInputPrm) {
		super(jobInputPrm);
	}

	public PrimitiveParameterInit(Parameter oldPrm) {
		super(oldPrm);
	}

	public PrimitiveParameterInit(ParameterType type) {
		super(type);
	}
	
	@Getter @Setter
	protected Parameter state;
}
