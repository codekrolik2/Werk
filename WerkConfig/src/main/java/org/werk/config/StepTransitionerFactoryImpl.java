package org.werk.config;

import java.lang.reflect.Constructor;

import org.werk.meta.StepTransitionerFactory;
import org.werk.processing.steps.StepTransitioner;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class StepTransitionerFactoryImpl<J> implements StepTransitionerFactory<J> {
	@SuppressWarnings("rawtypes")
	@Getter
	protected Class<StepTransitioner> stepTransitioner;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public StepTransitioner<J> createStepTransitioner() throws Exception {
		Constructor<StepTransitioner> constr = stepTransitioner.getConstructor();
		return (StepTransitioner<J>)constr.newInstance();
	}
}
