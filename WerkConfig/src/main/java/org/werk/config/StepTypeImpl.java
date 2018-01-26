package org.werk.config;

import java.util.List;

import org.werk.meta.StepExecFactory;
import org.werk.meta.StepTransitionerFactory;
import org.werk.meta.StepType;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class StepTypeImpl implements StepType {
	@Getter
	String stepTypeName;
	@Getter
	List<String> allowedTransitions;
	@Getter
	List<String> allowedRollbackTransitions;
	@Getter
	StepExecFactory stepExecFactory;
	@Getter
	StepTransitionerFactory stepTransitionerFactory;
	@Getter
	String processingDescription;
	@Getter
	String rollbackDescription;
	@Getter
	String execConfig;
	@Getter
	String transitionerConfig;
}
