package org.werk.engine.processing;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.werk.engine.processing.mapped.MappedParameter;
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

public class ParameterContext {
	protected Map<String, Parameter> parameters;
	protected Map<String, MappedParameter> removedMappedParameters;
	
	protected Parameter cloneParameter(Parameter parameter) {
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
	
	public ParameterContext cloneContext() {
		Map<String, Parameter> jobParameters0 = new HashMap<String, Parameter>();
		for (Map.Entry<String, Parameter> jobParameter : parameters.entrySet())
			jobParameters0.put(jobParameter.getKey(), cloneParameter(jobParameter.getValue()));
		
		return new ParameterContext(jobParameters0);
	}
	
	public ParameterContext(Map<String, Parameter> parameters) {
		this.parameters = parameters;
		removedMappedParameters = new HashMap<>();
	}
	
	public Map<String, Parameter> getParameters() {
		return Collections.unmodifiableMap(parameters);
	}

	public Parameter getParameter(String parameterName) {
		return parameters.get(parameterName);
	}

	public Parameter removeParameter(String parameterName) {
		Parameter parameter = parameters.remove(parameterName);
		
		if (parameter instanceof MappedParameter)
			removedMappedParameters.put(parameterName, (MappedParameter)parameter);
		
		return parameter;
	}

	public void putParameter(String parameterName, Parameter parameter) {
		Parameter oldPrm = parameters.get(parameterName);
		if (oldPrm == null) {
			oldPrm = removedMappedParameters.remove(parameterName);
			if (oldPrm != null)
				parameters.put(parameterName, oldPrm);
		}
		
		if ((oldPrm != null) && (oldPrm instanceof MappedParameter)) {
			((MappedParameter)oldPrm).update(parameter);
		} else
			parameters.put(parameterName, parameter);
	}
	
	//--------------------------------
	
	public Long getLongParameter(String parameterName) {
		return ((LongParameter)getParameter(parameterName)).getValue();
	}

	public void putLongParameter(String parameterName, Long value) {
		putParameter(parameterName, new LongParameterImpl(value));
	}
	
	public Double getDoubleParameter(String parameterName) {
		return ((DoubleParameter)getParameter(parameterName)).getValue();
	}

	public void putDoubleParameter(String parameterName, Double value) {
		putParameter(parameterName, new DoubleParameterImpl(value));
	}
	
	public Boolean getBoolParameter(String parameterName) {
		return ((BoolParameter)getParameter(parameterName)).getValue();
	}

	public void putBoolParameter(String parameterName, Boolean value) {
		putParameter(parameterName, new BoolParameterImpl(value));
	}
	
	public String getStringParameter(String parameterName) {
		return ((StringParameter)getParameter(parameterName)).getValue();
	}

	public void putStringParameter(String parameterName, String value) {
		putParameter(parameterName, new StringParameterImpl(value));
	}
	
	public Map<String, Parameter> getDictionaryParameter(String parameterName) {
		return ((DictionaryParameter)getParameter(parameterName)).getValue();
	}

	public void putDictionaryParameter(String parameterName, Map<String, Parameter> value) {
		putParameter(parameterName, new DictionaryParameterImpl(value));
	}
	
	public List<Parameter> getListParameter(String parameterName) {
		return ((ListParameter)getParameter(parameterName)).getValue();
	}

	public void putListParameter(String parameterName, List<Parameter> value) {
		putParameter(parameterName, new ListParameterImpl(value));
	}
}
