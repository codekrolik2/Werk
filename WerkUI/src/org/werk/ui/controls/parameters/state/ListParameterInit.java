package org.werk.ui.controls.parameters.state;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.werk.meta.inputparameters.JobInputParameter;
import org.werk.processing.parameters.Parameter;
import org.werk.processing.parameters.ParameterType;
import org.werk.processing.parameters.impl.ListParameterImpl;
import org.werk.ui.controls.parameters.ParameterInputFactory;

import lombok.Getter;
import lombok.Setter;

public class ListParameterInit extends ParameterInit {
	@Getter @Setter
	List<ParameterInit> listParametersState = new ArrayList<>();
	
	public ListParameterInit(JobInputParameter jobInputPrm) {
		super(jobInputPrm);
		
		parameterInput = ParameterInputFactory.createListParameterInput(this);
	}

	public ListParameterInit(Parameter oldPrm) {
		super(oldPrm);
		
		parameterInput = ParameterInputFactory.createListParameterInput(this);
	}

	public ListParameterInit() {
		super(ParameterType.LIST);
		
		parameterInput = ParameterInputFactory.createListParameterInput(this);
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
