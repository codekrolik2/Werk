package org.werk.ui.controls.parameters.state;

import java.util.ArrayList;
import java.util.List;

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
	
	public Parameter getState() throws ParameterStateException {
		if ((listParametersState == null) || (listParametersState.isEmpty()))
			new ListParameterImpl(null);
		
		List<Parameter> listParameters = new ArrayList<>();
		for (ParameterInit init : listParametersState)
			listParameters.add(init.getState());
		
		return new ListParameterImpl(listParameters);
	}
}
