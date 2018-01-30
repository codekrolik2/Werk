package org.werk.processing.steps;

public enum TransitionStatus {
	NEXT_STEP,
	ROLLBACK,
	FINISH,
	FINISH_ROLLBACK,
	FAIL
}
