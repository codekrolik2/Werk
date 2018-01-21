package com.github.bamirov.werk.meta;

import java.util.List;

import com.github.bamirov.werk.meta.inputparameters.JobInputParameter;

public interface JobType {
	String getJobTypeName();
	String getDescription();
	
	List<JobInputParameter> getInitInfo();
	List<StepType> getStepTypes();
	StepType getFirstStep();
}
