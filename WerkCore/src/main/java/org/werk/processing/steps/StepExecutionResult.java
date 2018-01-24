package org.werk.processing.steps;

import java.util.Optional;

public interface StepExecutionResult {
	StepExecutionStatus getStatus();
	Optional<Long> getDelayMS();
	Optional<Throwable> getException();
}
