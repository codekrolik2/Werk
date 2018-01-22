package org.werk.config;

import java.lang.reflect.Constructor;

import org.werk.meta.StepTransitionerFactory;
import org.werk.steps.StepTransitioner;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class StepTransitionerFactoryImpl implements StepTransitionerFactory {
	@Getter
	protected Class<StepTransitioner> stepTransitioner;

	@Override
	public StepTransitioner createStepTransitioner() throws Exception {
		Constructor<StepTransitioner> constr = stepTransitioner.getConstructor();
		return constr.newInstance();
	}
}
