package org.werk.engine.processing.mapped;

import java.lang.reflect.Field;

import org.werk.processing.parameters.DoubleParameter;
import org.werk.processing.parameters.Parameter;
import org.werk.processing.parameters.ParameterType;

public class MappedDoubleParameter extends MappedParameter implements DoubleParameter {
	public MappedDoubleParameter(Field field, Object objectInstance) {
		super(field, objectInstance);
	}

	@Override
	public ParameterType getType() {
		return ParameterType.DOUBLE;
	}

	@Override
	public double getValue() {
		try {
			return (double)getFieldValue();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void setValue(double val) {
		try {
			setFieldValue(val);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void update(Parameter parameter) {
		if (!(parameter instanceof DoubleParameter)) {
			throw new IllegalArgumentException(
				String.format("Mapped parameter of type DoubleParameter can't be assigned a value of type [%s]. Class [%s] field [%s]", 
					parameter.getClass(), objectInstance.getClass(), field.toString())
			);
		} else {
			setValue(((DoubleParameter)parameter).getValue());
		}
	}
}