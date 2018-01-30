package org.werk.config;

import java.lang.reflect.Constructor;

import org.werk.meta.StepTransitionerFactory;
import org.werk.processing.steps.Transitioner;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class StepTransitionerFactoryImpl<J> implements StepTransitionerFactory<J> {
	@SuppressWarnings("rawtypes")
	@Getter
	protected Class<Transitioner> stepTransitioner;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Transitioner<J> createStepTransitioner() throws Exception {
		Constructor<Transitioner> constr = stepTransitioner.getConstructor();
		return (Transitioner<J>)constr.newInstance();
	}
}
