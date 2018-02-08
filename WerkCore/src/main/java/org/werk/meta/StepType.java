package org.werk.meta;

import java.util.Set;

public interface StepType<J> {
	String getStepTypeName();
	
	Set<String> getAllowedTransitions();
	Set<String> getAllowedRollbackTransitions();
	
	StepExecFactory<J> getStepExecFactory();
	StepTransitionerFactory<J> getStepTransitionerFactory();

	//-------------------------
	
	String getProcessingDescription();
	String getRollbackDescription();

	String getExecConfig();
	String getTransitionerConfig();

	long getLogLimit();
	OverflowAction getLogOverflowAction();
	
	boolean isShortTransaction();
}
