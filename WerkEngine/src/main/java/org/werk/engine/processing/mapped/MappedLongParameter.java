package org.werk.engine.processing.mapped;

import java.lang.reflect.Field;

import org.werk.engine.WerkParameterException;
import org.werk.parameters.interfaces.LongParameter;
import org.werk.parameters.interfaces.Parameter;
import org.werk.parameters.interfaces.ParameterType;

public class MappedLongParameter extends MappedParameter implements LongParameter {
	public MappedLongParameter(Field field, Object objectInstance) {
		super(field, objectInstance);
	}

	@Override
	public ParameterType getType() {
		return ParameterType.LONG;
	}

	@Override
	public long getValue() {
		try {
			return (long)getFieldValue();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void setValue(long val) {
		try {
			setFieldValue(val);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void update(Parameter parameter) throws WerkParameterException {
		if (!(parameter instanceof LongParameter)) {
			throw new WerkParameterException(
				String.format("Mapped parameter of type LongParameter can't be assigned a value of type [%s]. Class [%s] field [%s]", 
					parameter.getClass(), objectInstance.getClass(), field.toString())
			);
		} else {
			setValue(((LongParameter)parameter).getValue());
		}
	}
}