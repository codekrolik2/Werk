package org.werk.steps;

public interface StepExecutionResult {
	StepExecutionStatus getStatus();
	long getDelayMS();
}
