package org.werk.config;

import java.util.Set;

import org.werk.meta.OverflowAction;
import org.werk.meta.StepExecFactory;
import org.werk.meta.StepTransitionerFactory;
import org.werk.meta.StepType;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class StepTypeImpl<J> implements StepType<J> {
	@Getter
	String stepTypeName;
	@Getter
	Set<String> allowedTransitions;
	@Getter
	Set<String> allowedRollbackTransitions;
	@Getter
	StepExecFactory<J> stepExecFactory;
	@Getter
	StepTransitionerFactory<J> stepTransitionerFactory;
	@Getter
	String processingDescription;
	@Getter
	String rollbackDescription;
	@Getter
	String execConfig;
	@Getter
	String transitionerConfig;
	@Getter
	long logLimit;
	@Getter
	OverflowAction logOverflowAction;
	@Getter
	boolean shortTransaction;
}
