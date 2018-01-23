package org.werk.engine.processing.mapped;

import java.lang.reflect.Field;
import java.util.List;

import org.werk.engine.WerkParameterException;
import org.werk.parameters.interfaces.ListParameter;
import org.werk.parameters.interfaces.Parameter;
import org.werk.parameters.interfaces.ParameterType;

public class MappedListParameter extends MappedParameter implements ListParameter {
	public MappedListParameter(Field field, Object objectInstance) {
		super(field, objectInstance);
	}

	@Override
	public ParameterType getType() {
		return ParameterType.LIST;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Parameter> getValue() {
		try {
			return (List<Parameter>)getFieldValue();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void setValue(List<Parameter> list) {
		try {
			setFieldValue(list);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void update(Parameter parameter) throws WerkParameterException {
		if (!(parameter instanceof ListParameter)) {
			throw new WerkParameterException(
				String.format("Mapped parameter of type ListParameter can't be assigned a value of type [%s]. Class [%s] field [%s]", 
					parameter.getClass(), objectInstance.getClass(), field.toString())
			);
		} else {
			setValue(((ListParameter)parameter).getValue());
		}
	}
}