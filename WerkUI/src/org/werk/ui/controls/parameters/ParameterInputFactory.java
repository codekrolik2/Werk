package org.werk.ui.controls.parameters;

import org.werk.processing.parameters.ParameterType;
import org.werk.ui.controls.parameters.state.DictionaryParameterInit;
import org.werk.ui.controls.parameters.state.ListParameterInit;
import org.werk.ui.controls.parameters.state.ParameterInit;
import org.werk.ui.controls.parameters.state.PrimitiveParameterInit;

public class ParameterInputFactory {
	public static ParameterInput createParameterInput(ParameterInit parameterInit) {
		if (parameterInit.getParameterType() == ParameterType.LONG)
			return new LongParameterInput((PrimitiveParameterInit)parameterInit);
		else if (parameterInit.getParameterType() == ParameterType.DOUBLE)
			return new DoubleParameterInput((PrimitiveParameterInit)parameterInit);
		else if (parameterInit.getParameterType() == ParameterType.BOOL)
			return new BooleanParameterInput((PrimitiveParameterInit)parameterInit);
		else if (parameterInit.getParameterType() == ParameterType.STRING)
			return new StringParameterInput((PrimitiveParameterInit)parameterInit);
		else if (parameterInit.getParameterType() == ParameterType.LIST)
			return new ListParameterInput((ListParameterInit)parameterInit);
		else if (parameterInit.getParameterType() == ParameterType.DICTIONARY)
			return new DictionaryParameterInput((DictionaryParameterInit)parameterInit);
		else 
			throw new IllegalArgumentException(
				String.format("Unknown ParameterType [%s]", parameterInit.getParameterType())
			);
	}
}
