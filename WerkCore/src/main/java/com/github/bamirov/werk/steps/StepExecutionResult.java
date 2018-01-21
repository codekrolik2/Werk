package com.github.bamirov.werk.steps;

public interface StepExecutionResult {
	StepExecutionStatus getStatus();
	long getDelayMS();
}
