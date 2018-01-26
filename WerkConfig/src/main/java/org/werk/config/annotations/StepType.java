package org.werk.config.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.werk.processing.steps.StepExec;
import org.werk.processing.steps.StepTransitioner;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface StepType {
	String name();
	
	String processingDescription();
	String rollbackDescription();
	
	Class<StepExec> stepExecClass();
	Class<StepTransitioner> stepTransitionerClass();

	String execConfig() default "";
	String transitionerConfig() default "";
}
