package org.werk.config.annotations.inputparameters;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.TYPE_PARAMETER})
public @interface DefaultDoubleParameter {
	boolean isDefaultValueImmutable();
	double defaultValue();
	
	String name() default "";
	String description() default "";
	boolean isOptional() default true;
}