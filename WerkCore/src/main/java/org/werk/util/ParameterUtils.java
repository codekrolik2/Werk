package org.werk.util;

import org.werk.processing.parameters.BoolParameter;
import org.werk.processing.parameters.DictionaryParameter;
import org.werk.processing.parameters.DoubleParameter;
import org.werk.processing.parameters.ListParameter;
import org.werk.processing.parameters.LongParameter;
import org.werk.processing.parameters.Parameter;
import org.werk.processing.parameters.StringParameter;
import org.werk.processing.parameters.impl.BoolParameterImpl;
import org.werk.processing.parameters.impl.DictionaryParameterImpl;
import org.werk.processing.parameters.impl.DoubleParameterImpl;
import org.werk.processing.parameters.impl.ListParameterImpl;
import org.werk.processing.parameters.impl.LongParameterImpl;
import org.werk.processing.parameters.impl.StringParameterImpl;

public class ParameterUtils {
	public static Object getParameterValue(Parameter ip) {
		switch (ip.getType()) {
			case LONG: return ((LongParameter)ip).getValue();
			case DOUBLE: return ((DoubleParameter)ip).getValue();
			case BOOL: return ((BoolParameter)ip).getValue();
			case STRING: return ((StringParameter)ip).getValue();
			
			case LIST: return ((ListParameter)ip).getValue();
			case DICTIONARY: return ((DictionaryParameter)ip).getValue();
		}
		
		throw new IllegalArgumentException(
			String.format("Unknown parameter type [%s]", ip.getType())
		);
	}
	
	public static Parameter cloneParameter(Parameter parameter) {
		if (parameter instanceof LongParameter) {
			return new LongParameterImpl(((LongParameter)parameter).getValue());
		} else if (parameter instanceof DoubleParameter) {
			return new DoubleParameterImpl(((DoubleParameter)parameter).getValue());
		} else if (parameter instanceof BoolParameter) {
			return new BoolParameterImpl(((BoolParameter)parameter).getValue());
		} else if (parameter instanceof StringParameter) {
			return new StringParameterImpl(((StringParameter)parameter).getValue());
		} else if (parameter instanceof ListParameter) {
			return new ListParameterImpl(((ListParameter)parameter).getValue());
		} else if (parameter instanceof DictionaryParameter) {
			return new DictionaryParameterImpl(((DictionaryParameter)parameter).getValue());
		} else
			throw new IllegalArgumentException(
				String.format("Unknown parameter type [%s]", parameter.getClass())
			);
	}
	
}
