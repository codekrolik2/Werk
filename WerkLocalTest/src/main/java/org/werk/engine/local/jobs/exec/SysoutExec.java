package org.werk.engine.local.jobs.exec;

import org.werk.config.annotations.inject.JobParameter;
import org.werk.processing.steps.ExecutionResult;
import org.werk.processing.steps.Step;
import org.werk.processing.steps.StepExec;

public class SysoutExec implements StepExec<Long> {
	@JobParameter(name="text")
	String text;
	
	@Override
	public ExecutionResult<Long> process(Step<Long> step) {
		System.out.println("PROCESSING: " + text + " " + step.getStepNumber());
		
		return ExecutionResult.success();
	}

	@Override
	public ExecutionResult<Long> rollback(Step<Long> step) {
		System.out.println("ROLLING BACK: " + text + " " + step.getStepNumber());
		
		return ExecutionResult.success();
	}
}
