package org.werk.meta;

import java.util.Set;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class StepTypeImpl<J> implements StepType<J> {
	@Getter
	protected String stepTypeName;
	@Getter @Setter
	protected JobTypeForStepGetter config;
	@Getter
	protected Set<String> allowedTransitions;
	@Getter
	protected Set<String> allowedRollbackTransitions;
	@Getter
	protected StepExecFactory<J> stepExecFactory;
	@Getter
	protected StepTransitionerFactory<J> stepTransitionerFactory;
	@Getter
	protected String processingDescription;
	@Getter
	protected String rollbackDescription;
	@Getter
	protected String execConfig;
	@Getter
	protected String transitionerConfig;
	@Getter
	protected long logLimit;
	@Getter
	protected OverflowAction logOverflowAction;
	@Getter
	protected boolean shortTransaction;
	@Override
	public Set<String> getJobTypes() {
		return config.getJobTypesForStep(stepTypeName).
			stream().map(a -> a.getJobTypeName()).
			collect(Collectors.toSet());
	}
}
