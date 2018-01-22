package org.werk.steps;

import java.util.Map;

import org.werk.jobs.Job;
import org.werk.parameters.interfaces.Parameter;

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
