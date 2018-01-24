package org.werk.engine.processing.mapped;

import java.lang.reflect.Field;

import org.werk.processing.parameters.Parameter;
import org.werk.processing.parameters.ParameterType;
import org.werk.processing.parameters.StringParameter;

public class MappedStringParameter extends MappedParameter implements StringParameter {
	public MappedStringParameter(Field field, Object objectInstance) {
		super(field, objectInstance);
	}

	@Override
	public ParameterType getType() {
		return ParameterType.STRING;
	}

	@Override
	public String getValue() {
		try {
			return (String)getFieldValue();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void setValue(String val) {
		try {
			setFieldValue(val);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void update(Parameter parameter) {
		if (!(parameter instanceof StringParameter)) {
			throw new IllegalArgumentException(
				String.format("Mapped parameter of type StringParameter can't be assigned a value of type [%s]. Class [%s] field [%s]", 
					parameter.getClass(), objectInstance.getClass(), field.toString())
			);
		} else {
			setValue(((StringParameter)parameter).getValue());
		}
	}
}