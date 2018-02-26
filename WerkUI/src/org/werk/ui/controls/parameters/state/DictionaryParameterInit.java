package org.werk.ui.controls.parameters.state;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.werk.meta.inputparameters.JobInputParameter;
import org.werk.processing.parameters.Parameter;
import org.werk.processing.parameters.ParameterType;
import org.werk.processing.parameters.impl.DictionaryParameterImpl;

import lombok.Getter;
import lombok.Setter;

public class DictionaryParameterInit extends ParameterInit {
	@Getter @Setter
	List<DictionaryParameterAndName> mapParametersState = new ArrayList<>();
	
	public DictionaryParameterInit(JobInputParameter jobInputPrm) {
		super(jobInputPrm);
	}

	public DictionaryParameterInit(Parameter oldPrm) {
		super(oldPrm);
	}

	public DictionaryParameterInit() {
		super(ParameterType.DICTIONARY);
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
