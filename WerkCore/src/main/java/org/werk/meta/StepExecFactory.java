package org.werk.meta;

import org.werk.processing.steps.StepExec;

public interface StepExecFactory {
	StepExec createStepExec() throws Exception;
}
