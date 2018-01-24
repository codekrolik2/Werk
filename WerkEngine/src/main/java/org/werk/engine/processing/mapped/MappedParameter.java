package org.werk.engine.processing.mapped;

import java.lang.reflect.Field;

import org.werk.processing.parameters.Parameter;

public abstract class MappedParameter implements Parameter {
	protected Field field;
	protected Object objectInstance;
	
	public MappedParameter(Field field, Object objectInstance) {
		this.field = field;
		field.setAccessible(true);
		this.objectInstance = objectInstance;
	}
	
	protected Object getFieldValue() throws IllegalArgumentException, IllegalAccessException {
		return field.get(objectInstance);
	}
	
	protected void setFieldValue(Object value) throws IllegalArgumentException, IllegalAccessException {
		field.set(objectInstance, value);
	}
	
	public abstract void update(Parameter parameter);
}
