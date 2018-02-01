package org.werk.config.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.werk.meta.OverflowAction;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface JobType {
	String name();
	String description() default "";
	
	String firstStepTypeName();
	String[] stepTypeNames();

	boolean forceAcyclic() default false;
	long version() default 1;

	String jobConfig() default "";
	
	long historyLimit() default 50;
	OverflowAction historyOverflowAction() default OverflowAction.FAIL;
}
