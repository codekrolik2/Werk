package org.werk.ui.controls.parameters.state;

import java.util.List;
import java.util.stream.Collectors;

import org.werk.meta.inputparameters.JobInputParameter;
import org.werk.processing.parameters.Parameter;
import org.werk.processing.parameters.ParameterType;
import org.werk.processing.parameters.impl.DictionaryParameterImpl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

public class DictionaryParameterInit extends ParameterInit {
	@AllArgsConstructor
	public static class ParameterInitTuple {
		@Getter @Setter
		String name;
		@Getter @Setter
		ParameterInit init;
	}
	
	@Getter @Setter
	List<ParameterInitTuple> mapParametersState;
	
	public DictionaryParameterInit(JobInputParameter jobInputPrm) {
		super(jobInputPrm);
	}

	public DictionaryParameterInit(Parameter oldPrm) {
		super(oldPrm);
	}

	public DictionaryParameterInit(ParameterType type) {
		super(type);
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
