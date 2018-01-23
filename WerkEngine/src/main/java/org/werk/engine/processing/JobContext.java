package org.werk.engine.processing;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.werk.engine.WerkParameterException;
import org.werk.engine.processing.mapped.MappedParameter;
import org.werk.parameters.interfaces.Parameter;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class JobContext {
	Map<String, Parameter> jobParameters;
	
	JobContext cloneContext() {
		Map<String, Parameter> jobParameters0 = new HashMap<String, Parameter>(jobParameters);
		return new JobContext(jobParameters0);
	}
	
	public Map<String, Parameter> getJobParameters() {
		return Collections.unmodifiableMap(jobParameters);
	}

	public Parameter getJobParameter(String parameterName) {
		return jobParameters.get(parameterName);
	}

	public Parameter removeJobParameter(String parameterName) {
		return jobParameters.remove(parameterName);
	}

	public void putJobParameter(String parameterName, Parameter parameter) {
		Parameter oldPrm = jobParameters.get(parameterName);
		
		if (oldPrm instanceof MappedParameter) {
			try {
				((MappedParameter)oldPrm).update(parameter);
			} catch (WerkParameterException e) {
				throw new RuntimeException(e);
			}
		} else
			jobParameters.put(parameterName, parameter);
	}
}
