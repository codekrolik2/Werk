package org.werk.config;

import java.lang.reflect.Constructor;

import org.werk.meta.StepExecFactory;
import org.werk.processing.steps.StepExec;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class StepExecFactoryImpl implements StepExecFactory {
	@Getter
	protected Class<StepExec> stepExec;

	@Override
	public StepExec createStepExec() throws Exception {
		Constructor<StepExec> constr = stepExec.getConstructor();
		return constr.newInstance();
	}
}
