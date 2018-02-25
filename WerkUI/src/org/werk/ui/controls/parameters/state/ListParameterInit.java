package org.werk.ui.controls.parameters.state;

import java.util.List;
import java.util.stream.Collectors;

import org.werk.meta.inputparameters.JobInputParameter;
import org.werk.processing.parameters.Parameter;
import org.werk.processing.parameters.ParameterType;
import org.werk.processing.parameters.impl.ListParameterImpl;

import lombok.Getter;
import lombok.Setter;

public class ListParameterInit extends ParameterInit {
	@Getter @Setter
	List<ParameterInit> listParametersState;
	
	public ListParameterInit(JobInputParameter jobInputPrm) {
		super(jobInputPrm);
	}

	public ListParameterInit(Parameter oldPrm) {
		super(oldPrm);
	}

	public ListParameterInit(ParameterType type) {
		super(type);
	}
	
	public Parameter getState() {
		if (listParametersState == null)
			return null;
		return new ListParameterImpl(
				listParametersState.stream().
				map(a -> a.getState()).
				collect(Collectors.toList())
			);
	}
}
