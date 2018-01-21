package com.github.bamirov.werk.steps;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class StepExecutionResultImpl implements StepExecutionResult {
	@Getter
	protected StepExecutionStatus status;
	@Getter
	protected long delayMS;
}
