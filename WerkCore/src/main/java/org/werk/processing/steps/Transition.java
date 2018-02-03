package org.werk.processing.steps;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access=AccessLevel.PRIVATE)
public final class Transition {
	@Getter
	protected final TransitionStatus transitionStatus;
	@Getter
	protected final Optional<String> stepName;
	@Getter
	protected final Optional<Long> delayMS;
	@Getter
	protected final List<Long> rollbackStepNumbers;
	
	public static Transition nextStep(String stepName) {
		return new Transition(TransitionStatus.NEXT_STEP, Optional.of(stepName), Optional.empty(), new ArrayList<Long>());
	}
	
	public static Transition nextStep(String stepName, long delayMS) {
		return new Transition(TransitionStatus.NEXT_STEP, Optional.of(stepName), Optional.of(delayMS), new ArrayList<Long>());
	}
	
	public static Transition rollback(String stepName) {
		return new Transition(TransitionStatus.ROLLBACK, Optional.of(stepName), Optional.empty(), new ArrayList<Long>());
	}
	
	public static Transition rollback(String stepName, long delayMS) {
		return new Transition(TransitionStatus.ROLLBACK, Optional.of(stepName), Optional.of(delayMS), new ArrayList<Long>());
	}
	
	public static Transition rollback(String stepName, List<Long> rollbackStepNumbers) {
		return new Transition(TransitionStatus.ROLLBACK, Optional.of(stepName), Optional.empty(), rollbackStepNumbers);
	}
	
	public static Transition rollback(String stepName, List<Long> rollbackStepNumbers, long delayMS) {
		return new Transition(TransitionStatus.ROLLBACK, Optional.of(stepName), Optional.of(delayMS), rollbackStepNumbers);
	}
	
	public static Transition finish() {
		return new Transition(TransitionStatus.FINISH, Optional.empty(), Optional.empty(), new ArrayList<Long>());
	}
	
	public static Transition finishRollback() {
		return new Transition(TransitionStatus.FINISH_ROLLBACK, Optional.empty(), Optional.empty(), new ArrayList<Long>());
	}
	
	public static Transition fail() {
		return new Transition(TransitionStatus.FAIL, Optional.empty(), Optional.empty(), new ArrayList<Long>());
	}
}
