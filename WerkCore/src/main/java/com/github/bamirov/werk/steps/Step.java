package com.github.bamirov.werk.steps;

import java.util.Map;

import com.github.bamirov.werk.jobs.Job;
import com.github.bamirov.werk.parameters.interfaces.Parameter;

public interface Step {
	Job getJob();
	String getStepTypeName();
	
	Map<String, Parameter> getStepParameters();
	Parameter getStepParameter(String parameterName);
	Parameter removeStepParameter(String parameterName);
	void putStepParameter(String parameterName, Parameter parameter);
	
	StepExec getStepExec();
	StepTransitioner getStepTransitioner();
	
	long getExecutionCount();
}
