package org.werk.meta;

import java.util.List;

public interface StepType<J> {
	String getStepTypeName();
	
	List<String> getAllowedTransitions();
	List<String> getAllowedRollbackTransitions();
	
	StepExecFactory<J> getStepExecFactory();
	StepTransitionerFactory<J> getStepTransitionerFactory();

	//-------------------------
	
	String getProcessingDescription();
	String getRollbackDescription();

	String getExecConfig();
	String getTransitionerConfig();
}
