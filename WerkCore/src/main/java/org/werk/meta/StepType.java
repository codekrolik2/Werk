package org.werk.meta;

import java.util.List;

public interface StepType {
	String getStepTypeName();
	
	List<String> getAllowedTransitions();
	List<String> getAllowedRollbackTransitions();
	
	StepExecFactory getStepExecFactory();
	StepTransitionerFactory getStepTransitionerFactory();

	//-------------------------
	
	String getProcessingDescription();
	String getRollbackDescription();

	String getExecConfig();
	String getTransitionerConfig();
}
