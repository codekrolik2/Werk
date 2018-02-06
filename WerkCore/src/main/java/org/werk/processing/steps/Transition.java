package org.werk.processing.steps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.werk.processing.parameters.Parameter;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access=AccessLevel.PRIVATE)
public final class Transition {
	@Getter
	protected final TransitionStatus transitionStatus;
	@Getter
	protected final Optional<String> stepTypeName;
	@Getter
	protected final Optional<Long> delayMS;
	@Getter
	protected final List<Integer> rollbackStepNumbers;
	@Getter
	protected final Map<String, Parameter> rollbackStepParameters;
	
	public static Transition nextStep(String stepTypeName) {
		return new Transition(TransitionStatus.NEXT_STEP, Optional.of(stepTypeName), Optional.empty(), 
				new ArrayList<>(), new HashMap<>());
	}
	
	public static Transition nextStep(String stepTypeName, long delayMS) {
		return new Transition(TransitionStatus.NEXT_STEP, Optional.of(stepTypeName), Optional.of(delayMS), 
				new ArrayList<Integer>(), new HashMap<>());
	}
	
	public static Transition rollback(String stepTypeName) {
		return new Transition(TransitionStatus.ROLLBACK, Optional.of(stepTypeName), Optional.empty(), 
				new ArrayList<Integer>(), new HashMap<>());
	}
	
	public static Transition rollback(String stepTypeName, long delayMS) {
		return new Transition(TransitionStatus.ROLLBACK, Optional.of(stepTypeName), Optional.of(delayMS), 
				new ArrayList<Integer>(), new HashMap<>());
	}
	
	public static Transition rollback(String stepTypeName, List<Integer> rollbackStepNumbers) {
		return new Transition(TransitionStatus.ROLLBACK, Optional.of(stepTypeName), Optional.empty(), 
				rollbackStepNumbers, new HashMap<>());
	}
	
	public static Transition rollback(String stepTypeName, List<Integer> rollbackStepNumbers, long delayMS) {
		return new Transition(TransitionStatus.ROLLBACK, Optional.of(stepTypeName), Optional.of(delayMS), 
				rollbackStepNumbers, new HashMap<>());
	}
	
	public static Transition rollback(String stepTypeName, List<Integer> rollbackStepNumbers, 
			Map<String, Parameter> rollbackStepParameters) {
		return new Transition(TransitionStatus.ROLLBACK, Optional.of(stepTypeName), Optional.empty(), 
				rollbackStepNumbers, rollbackStepParameters);
	}
	
	public static Transition rollback(String stepTypeName, List<Integer> rollbackStepNumbers, 
			Map<String, Parameter> rollbackStepParameters, long delayMS) {
		return new Transition(TransitionStatus.ROLLBACK, Optional.of(stepTypeName), Optional.of(delayMS), 
				rollbackStepNumbers, rollbackStepParameters);
	}
	
	public static Transition finish() {
		return new Transition(TransitionStatus.FINISH, Optional.empty(), Optional.empty(), 
				new ArrayList<Integer>(), new HashMap<>());
	}
	
	public static Transition finishRollback() {
		return new Transition(TransitionStatus.FINISH_ROLLBACK, Optional.empty(), Optional.empty(), 
				new ArrayList<Integer>(), new HashMap<>());
	}
	
	public static Transition fail() {
		return new Transition(TransitionStatus.FAIL, Optional.empty(), Optional.empty(), 
				new ArrayList<Integer>(), new HashMap<>());
	}
}
