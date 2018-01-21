package com.github.bamirov.werk.meta;

import java.util.List;

import com.github.bamirov.werk.steps.StepExec;
import com.github.bamirov.werk.steps.StepTransitioner;

public interface StepType {
	String getStepTypeName();
	
	String getProcessingDescription();
	String getRollbackDescription();
	
	List<String> getAllowedTransitions();
	List<String> getAllowedRollbackTransitions();
	
	Factory<StepExec> getStepExecFactory();
	Factory<StepTransitioner> getStepTransitionerFactory();
}
