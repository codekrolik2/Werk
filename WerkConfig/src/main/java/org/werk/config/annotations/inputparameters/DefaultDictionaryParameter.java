package org.werk.config.annotations.inputparameters;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.TYPE_PARAMETER})
public @interface DefaultDictionaryParameter {
	boolean isDefaultValueImmutable();
	String dictionaryValueGetterMethod();
	
	String name();
	String description() default "";
}