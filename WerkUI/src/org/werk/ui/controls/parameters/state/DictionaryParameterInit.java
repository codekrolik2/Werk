package org.werk.ui.controls.parameters.state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
	
	boolean topLevel = false;
	
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
		this.topLevel = topLevel;
		if (!topLevel)
			parameterInput = ParameterInputFactory.createDictionaryParameterInput(this, 
					DictionaryParameterInputType.INNER);
	}
	
	public Parameter getState() throws ParameterStateException {
		if ((mapParametersState == null) || (mapParametersState.isEmpty()))
			return new DictionaryParameterImpl(null);
		
		Map<String, Parameter> values = new HashMap<>();
		for (DictionaryParameterAndName dpn : mapParametersState) {
			String key = dpn.getName().trim();
			if (key.equals(""))
				throw new ParameterStateException("Empty key in DICTIONARY: " + dpn.getName());
			
			if (values.containsKey(key))
				throw new ParameterStateException("Duplicate key in DICTIONARY: " + dpn.getName());
			
			if (!dpn.getInit().isImmutable())
				values.put(key, dpn.getInit().getState());
		}
		
		return new DictionaryParameterImpl(values);
	}
}
