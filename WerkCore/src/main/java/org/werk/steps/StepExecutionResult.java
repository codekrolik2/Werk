package org.werk.steps;

import java.util.Optional;

public interface StepExecutionResult {
	StepExecutionStatus getStatus();
	Optional<Long> getDelayMS();
}
