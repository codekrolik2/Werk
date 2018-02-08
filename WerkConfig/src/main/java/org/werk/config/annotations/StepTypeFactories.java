package org.werk.config.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.werk.meta.OverflowAction;
import org.werk.meta.StepExecFactory;
import org.werk.meta.StepTransitionerFactory;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface StepTypeFactories {
	String name();
	String processingDescription();
	String rollbackDescription();
	
	@SuppressWarnings("rawtypes")
	Class<StepExecFactory> stepExecFactoryClass();
	@SuppressWarnings("rawtypes")
	Class<StepTransitionerFactory> stepTransitionerFactoryClass();

	String execConfig() default "";
	String transitionerConfig() default "";

	String[] transitions();
	String[] rollbackTransitions();

	long logLimit() default 50;
	OverflowAction logOverflowAction() default OverflowAction.FAIL;
	
	boolean shortTransaction() default false;
}
