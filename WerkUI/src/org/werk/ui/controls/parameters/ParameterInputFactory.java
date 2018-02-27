package org.werk.ui.controls.parameters;

import org.werk.processing.parameters.ParameterType;
import org.werk.ui.controls.parameters.state.DictionaryParameterInit;
import org.werk.ui.controls.parameters.state.ListParameterInit;
import org.werk.ui.controls.parameters.state.PrimitiveParameterInit;

public class ParameterInputFactory {
	public static ParameterInput createParameterInput(PrimitiveParameterInit parameterInit) {
		if (parameterInit.getParameterType() == ParameterType.LONG)
			return new LongParameterInput((PrimitiveParameterInit)parameterInit);
		else if (parameterInit.getParameterType() == ParameterType.DOUBLE)
			return new DoubleParameterInput((PrimitiveParameterInit)parameterInit);
		else if (parameterInit.getParameterType() == ParameterType.BOOL)
			return new BooleanParameterInput((PrimitiveParameterInit)parameterInit);
		else if (parameterInit.getParameterType() == ParameterType.STRING)
			return new StringParameterInput((PrimitiveParameterInit)parameterInit);
		else 
			throw new IllegalArgumentException(
				String.format("Unknown Primitive ParameterType [%s]", parameterInit.getParameterType())
			);
	}
	
	public static ParameterInput createListParameterInput(ListParameterInit parameterInit) {
		return new ListParameterInput(parameterInit);
	}
	
	public static ParameterInput createDictionaryParameterInput(DictionaryParameterInit parameterInit, 
			DictionaryParameterInputType dictType) {
		return new DictionaryParameterInput(parameterInit, dictType);
	}
}
