package org.werk.config.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.werk.steps.StepExec;
import org.werk.steps.StepTransitioner;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface StepType {
	String getName();
	String[] getJobNames();
	String getProcessingDescription();
	String getRollbackDescription();
	
	Class<StepExec> getStepExecClass();
	Class<StepTransitioner> getStepTransitionerClass();

	String customInfo() default "";
}
