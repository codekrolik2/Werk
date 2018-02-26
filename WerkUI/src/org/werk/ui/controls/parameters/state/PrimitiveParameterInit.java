package org.werk.ui.controls.parameters.state;

import org.werk.meta.inputparameters.JobInputParameter;
import org.werk.processing.parameters.Parameter;
import org.werk.processing.parameters.ParameterType;
import org.werk.processing.parameters.impl.BoolParameterImpl;
import org.werk.processing.parameters.impl.DoubleParameterImpl;
import org.werk.processing.parameters.impl.LongParameterImpl;
import org.werk.processing.parameters.impl.StringParameterImpl;

import lombok.Getter;
import lombok.Setter;

public class PrimitiveParameterInit extends ParameterInit {
	public PrimitiveParameterInit(JobInputParameter jobInputPrm) {
		super(jobInputPrm);
		setDefaultState(jobInputPrm.getType());
	}

	public PrimitiveParameterInit(Parameter oldPrm) {
		super(oldPrm);
		setDefaultState(oldPrm.getType());
	}

	public PrimitiveParameterInit(ParameterType type) {
		super(type);
		setDefaultState(type);
	}
	
	public void setDefaultState(ParameterType type) {
		if (type == ParameterType.BOOL)
			state = new BoolParameterImpl(null);
		else if (type == ParameterType.DOUBLE)
			state = new DoubleParameterImpl(null);
		else if (type == ParameterType.LONG)
			state = new LongParameterImpl(null);
		else if (type == ParameterType.STRING)
			state = new StringParameterImpl(null);
		else
			throw new IllegalArgumentException(
				String.format("Parameter type not supported by PrimitiveParameterInit [%s]", type)
			);
	}
	
	@Getter @Setter
	protected Parameter state;
}
