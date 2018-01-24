package org.werk.engine.processing.mapped;

import java.lang.reflect.Field;
import java.util.Map;

import org.werk.processing.parameters.DictionaryParameter;
import org.werk.processing.parameters.Parameter;
import org.werk.processing.parameters.ParameterType;

public class MappedDictionaryParameter extends MappedParameter implements DictionaryParameter {
	public MappedDictionaryParameter(Field field, Object objectInstance) {
		super(field, objectInstance);
	}

	@Override
	public ParameterType getType() {
		return ParameterType.DICTIONARY;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Parameter> getValue() {
		try {
			return (Map<String, Parameter>)getFieldValue();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void setValue(Map<String, Parameter> map) {
		try {
			setFieldValue(map);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public void update(Parameter parameter) {
		if (!(parameter instanceof DictionaryParameter)) {
			throw new IllegalArgumentException(
				String.format("Mapped parameter of type DictionaryParameter can't be assigned a value of type [%s]. Class [%s] field [%s]", 
					parameter.getClass(), objectInstance.getClass(), field.toString())
			);
		} else {
			setValue(((DictionaryParameter)parameter).getValue());
		}
	}
}