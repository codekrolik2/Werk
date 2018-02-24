package org.werk.config;

import java.lang.reflect.Constructor;

import org.werk.meta.StepExecFactory;
import org.werk.processing.steps.StepExec;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class StepExecFactoryImpl<J> implements StepExecFactory<J> {
	@SuppressWarnings("rawtypes")
	@Getter
	protected Class<StepExec> stepExec;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public StepExec<J> createStepExec() throws Exception {
		Constructor<StepExec> constr = stepExec.getConstructor();
		return (StepExec<J>)constr.newInstance();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Class getStepExecClass() {
		return stepExec;
	}

	@Override
	public String getStepExecClassName() {
		return stepExec.getName();
	}
}
