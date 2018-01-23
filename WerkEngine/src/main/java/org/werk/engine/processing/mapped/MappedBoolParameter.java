package org.werk.engine.processing.mapped;

import java.lang.reflect.Field;

import org.werk.engine.WerkParameterException;
import org.werk.parameters.interfaces.BoolParameter;
import org.werk.parameters.interfaces.Parameter;
import org.werk.parameters.interfaces.ParameterType;

public class MappedBoolParameter extends MappedParameter implements BoolParameter {
	public MappedBoolParameter(Field field, Object objectInstance) {
		super(field, objectInstance);
	}

	@Override
	public ParameterType getType() {
		return ParameterType.BOOL;
	}

	@Override
	public boolean isValue() {
		try {
			return (boolean)getFieldValue();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void setValue(boolean val) {
		try {
			setFieldValue(val);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public void update(Parameter parameter) throws WerkParameterException {
		if (!(parameter instanceof BoolParameter)) {
			throw new WerkParameterException(
				String.format("Mapped parameter of type BoolParameter can't be assigned a value of type [%s]. Class [%s] field [%s]", 
					parameter.getClass(), objectInstance.getClass(), field.toString())
			);
		} else {
			setValue(((BoolParameter)parameter).isValue());
		}
	}
}