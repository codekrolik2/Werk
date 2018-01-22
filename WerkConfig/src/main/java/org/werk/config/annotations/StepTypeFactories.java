package org.werk.config.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.werk.meta.StepExecFactory;
import org.werk.meta.StepTransitionerFactory;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface StepTypeFactories {
	String getName();
	String[] getJobNames();
	String getProcessingDescription();
	String getRollbackDescription();
	
	Class<StepExecFactory> getStepExecFactoryClass();
	Class<StepTransitionerFactory> getStepTransitionerFactoryClass();
}
