package org.werk.ui.controls.parameters.state;

import java.util.Optional;

import org.werk.meta.inputparameters.JobInputParameter;
import org.werk.processing.parameters.Parameter;
import org.werk.processing.parameters.ParameterType;

import lombok.Getter;

public abstract class ParameterInit {
	@Getter
	protected Optional<JobInputParameter> jobInputParameter;
	@Getter
	protected Optional<Parameter> oldParameter;
	protected ParameterType type;
	
	public ParameterInit(ParameterType type) {
		jobInputParameter = Optional.empty();
		oldParameter = Optional.empty();
		this.type = type;
	}
	
	public ParameterInit(JobInputParameter jobInputPrm) {
		jobInputParameter = Optional.of(jobInputPrm);
		oldParameter = Optional.empty();
		this.type = null;
	}
	
	public ParameterInit(Parameter oldPrm) {
		jobInputParameter = Optional.empty();
		oldParameter = Optional.of(oldPrm);
		this.type = null;
	}
	
	public ParameterType getParameterType() {
		if (jobInputParameter.isPresent())
			return jobInputParameter.get().getType();
		else if (oldParameter.isPresent())
			return oldParameter.get().getType();
		else
			return type;
	}
	
	public abstract Parameter getState();
}
