package org.werk.ui.controls.parameters.state;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.werk.meta.inputparameters.JobInputParameter;
import org.werk.processing.parameters.Parameter;
import org.werk.processing.parameters.ParameterType;
import org.werk.processing.parameters.impl.DictionaryParameterImpl;
import org.werk.ui.controls.parameters.DictionaryParameterInputType;
import org.werk.ui.controls.parameters.ParameterInputFactory;

import lombok.Getter;
import lombok.Setter;

public class DictionaryParameterInit extends ParameterInit {
	@Getter @Setter
	List<DictionaryParameterAndName> mapParametersState = new ArrayList<>();
	@Getter
	Optional<List<JobInputParameter>> inputParameters;
	
	public DictionaryParameterInit(Optional<List<JobInputParameter>> inputParameters, boolean topLevel) {
		super(ParameterType.DICTIONARY);
		this.inputParameters = inputParameters;
		
		createInput(topLevel);
	}
	
	public DictionaryParameterInit(JobInputParameter jobInputPrm, boolean topLevel) {
		super(jobInputPrm);
		inputParameters = Optional.empty();
		
		createInput(topLevel);
	}

	public DictionaryParameterInit(Parameter oldPrm, boolean topLevel) {
		super(oldPrm);
		inputParameters = Optional.empty();
		
		createInput(topLevel);
	}

	public DictionaryParameterInit(boolean topLevel) {
		super(ParameterType.DICTIONARY);
		inputParameters = Optional.empty();
		
		createInput(topLevel);
	}

	public DictionaryParameterInit(List<DictionaryParameterAndName> mapParametersState, boolean topLevel) {
		super(ParameterType.DICTIONARY);
		inputParameters = Optional.empty();
		this.mapParametersState = mapParametersState;
		
		createInput(topLevel);
	}
	
	protected void createInput(boolean topLevel) {
		if (!topLevel)
			parameterInput = ParameterInputFactory.createDictionaryParameterInput(this, 
					DictionaryParameterInputType.INNER);
	}
	
	public Parameter getState() {
		if (mapParametersState == null)
			return null;
		
		return new DictionaryParameterImpl(
			mapParametersState.stream().
			collect(Collectors.toMap(
					a -> a.getName(), 
					a -> a.getInit().getState()
				)
			)
		);
	}
}
