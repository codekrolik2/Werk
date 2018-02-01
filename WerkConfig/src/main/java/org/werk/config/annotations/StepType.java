package org.werk.config.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.werk.meta.OverflowAction;
import org.werk.processing.steps.SimpleTransitioner;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface StepType {
	String name();
	
	String processingDescription() default "";
	String rollbackDescription() default "";
	
	@SuppressWarnings("rawtypes")
	Class stepExecClass();
	@SuppressWarnings("rawtypes")
	Class stepTransitionerClass() default SimpleTransitioner.class;

	String execConfig() default "";
	String transitionerConfig() default "";
	
	String[] transitions();
	String[] rollbackTransitions();
	
	long logLimit() default 50;
	OverflowAction logOverflowAction() default OverflowAction.FAIL;
}
