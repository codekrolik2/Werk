package org.werk.config.annotations;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.werk.config.annotations.inputparameters.DefaultBoolParameter;
import org.werk.config.annotations.inputparameters.DefaultDictionaryParameter;
import org.werk.config.annotations.inputparameters.DefaultDoubleParameter;
import org.werk.config.annotations.inputparameters.DefaultListParameter;
import org.werk.config.annotations.inputparameters.DefaultLongParameter;
import org.werk.config.annotations.inputparameters.DefaultStringParameter;
import org.werk.config.annotations.inputparameters.EnumDoubleParameter;
import org.werk.config.annotations.inputparameters.EnumLongParameter;
import org.werk.config.annotations.inputparameters.EnumStringParameter;
import org.werk.config.annotations.inputparameters.JobInputParameter;
import org.werk.config.annotations.inputparameters.RangeDoubleParameter;
import org.werk.config.annotations.inputparameters.RangeLongParameter;
import org.werk.config.annotations.inputparameters.RangeStringParameter;
import org.werk.exceptions.WerkConfigException;
import org.werk.meta.inputparameters.DefaultValueJobInputParameter;
import org.werk.meta.inputparameters.EnumJobInputParameter;
import org.werk.meta.inputparameters.RangeJobInputParameter;
import org.werk.meta.inputparameters.impl.DefaultValueJobInputParameterImpl;
import org.werk.meta.inputparameters.impl.EnumJobInputParameterImpl;
import org.werk.meta.inputparameters.impl.JobInputParameterImpl;
import org.werk.meta.inputparameters.impl.RangeJobInputParameterImpl;
import org.werk.processing.parameters.BoolParameter;
import org.werk.processing.parameters.DictionaryParameter;
import org.werk.processing.parameters.DoubleParameter;
import org.werk.processing.parameters.ListParameter;
import org.werk.processing.parameters.LongParameter;
import org.werk.processing.parameters.ParameterType;
import org.werk.processing.parameters.StringParameter;
import org.werk.processing.parameters.impl.BoolParameterImpl;
import org.werk.processing.parameters.impl.DictionaryParameterImpl;
import org.werk.processing.parameters.impl.DoubleParameterImpl;
import org.werk.processing.parameters.impl.ListParameterImpl;
import org.werk.processing.parameters.impl.LongParameterImpl;
import org.werk.processing.parameters.impl.StringParameterImpl;

public class JobInputParameterLoader {
	@SuppressWarnings({ "rawtypes" })
	public org.werk.meta.inputparameters.JobInputParameter 
		loadInputParameter(Parameter param, String clazz, String method) throws WerkConfigException, 
			NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, 
			InvocationTargetException {
		Class paramClass = param.getType();
		
		JobInputParameter jobInputParameter = null;
		try {
			jobInputParameter = (JobInputParameter)param.getAnnotation(JobInputParameter.class);
		} catch (NullPointerException npe) {}
		if (jobInputParameter != null)
			return loadJobInputParameter(param, clazz, method, paramClass, jobInputParameter);
		
		DefaultLongParameter defaultLongParameter = null;
		try {
			defaultLongParameter = (DefaultLongParameter)param.getAnnotation(DefaultLongParameter.class);
		} catch (NullPointerException e) {}
		if (defaultLongParameter != null)
			return loadDefaultLongParameter(param, clazz, method, paramClass, defaultLongParameter);
		
		DefaultDoubleParameter defaultDoubleParameter = null;
		try {
			defaultDoubleParameter = (DefaultDoubleParameter)param.getAnnotation(DefaultDoubleParameter.class);
		} catch (NullPointerException e) {}
		if (defaultDoubleParameter != null)
			return loadDefaultDoubleParameter(param, clazz, method, paramClass, defaultDoubleParameter);
		
		DefaultBoolParameter defaultBoolParameter = null;
		try {
			defaultBoolParameter = (DefaultBoolParameter)param.getAnnotation(DefaultBoolParameter.class);
		} catch (NullPointerException e) {}
		if (defaultBoolParameter != null)
			return loadDefaultBoolParameter(param, clazz, method, paramClass, defaultBoolParameter);
		
		DefaultStringParameter defaultStringParameter = null;
		try {
			defaultStringParameter = (DefaultStringParameter)param.getAnnotation(DefaultStringParameter.class);
		} catch (NullPointerException npe) {}
		if (defaultStringParameter != null)
			return loadDefaultStringParameter(param, clazz, method, paramClass, defaultStringParameter);
		
		DefaultListParameter defaultListParameter = null;
		try {
			defaultListParameter = (DefaultListParameter)param.getAnnotation(DefaultListParameter.class);
		} catch (NullPointerException npe) {}
		if (defaultListParameter != null)
			return loadDefaultListParameter(param, clazz, method, paramClass, defaultListParameter); 
		
		DefaultDictionaryParameter defaultDictionaryParameter = null;
		try {
			defaultDictionaryParameter = (DefaultDictionaryParameter)param.getAnnotation(DefaultDictionaryParameter.class);
		} catch (NullPointerException npe) {}
		if (defaultDictionaryParameter != null)
			return loadDefaultDictionaryParameter(param, clazz, method, paramClass, defaultDictionaryParameter); 
		
		EnumDoubleParameter enumDoubleParameter = null;
		try {
			enumDoubleParameter = (EnumDoubleParameter)param.getAnnotation(EnumDoubleParameter.class);
		} catch (NullPointerException npe) {}
		if (enumDoubleParameter != null)
			return loadEnumDoubleParameter(param, clazz, method, paramClass, enumDoubleParameter);

		EnumLongParameter enumLongParameter = null;
		try {
			enumLongParameter = (EnumLongParameter)param.getAnnotation(EnumLongParameter.class);
		} catch (NullPointerException npe) {}
		if (enumLongParameter != null)
			return loadEnumLongParameter(param, clazz, method, paramClass, enumLongParameter);

		EnumStringParameter enumStringParameter = null;
		try {
			enumStringParameter = (EnumStringParameter)param.getAnnotation(EnumStringParameter.class);
		} catch (NullPointerException npe) {}
		if (enumStringParameter != null)
			return loadEnumStringParameter(param, clazz, method, paramClass, enumStringParameter);

		RangeDoubleParameter rangeDoubleParameter = null;
		try {
			rangeDoubleParameter = (RangeDoubleParameter)param.getAnnotation(RangeDoubleParameter.class);
		} catch (NullPointerException npe) {}
		if (rangeDoubleParameter != null)
			return loadRangeDoubleParameter(param, clazz, method, paramClass, rangeDoubleParameter);

		RangeLongParameter rangeLongParameter = null;
		try {
			rangeLongParameter = (RangeLongParameter)param.getAnnotation(RangeLongParameter.class);
		} catch (NullPointerException npe) {}
		if (rangeLongParameter != null)
			return loadRangeLongParameter(param, clazz, method, paramClass, rangeLongParameter);

		RangeStringParameter rangeStringParameter = null;
		try {
			rangeStringParameter = (RangeStringParameter)param.getAnnotation(RangeStringParameter.class);
		} catch (NullPointerException npe) {}
		if (rangeStringParameter != null)
			return loadRangeStringParameter(param, clazz, method, paramClass, rangeStringParameter);
		
		throw new WerkConfigException(
			String.format("Class [%s] Method [%s] is annotated as @JobInit, but its Parameter #%d is not annotated as a job input parameter ", 
					clazz, method, param.getName())
		);
	}
	
	//------------RANGES------------
	
	@SuppressWarnings({ "rawtypes" })
	protected RangeJobInputParameter loadRangeLongParameter(Parameter param, String clazz,
			String method, Class paramClass, RangeLongParameter rangeLongParameter) throws WerkConfigException {
		if (paramClass.equals(int.class) || paramClass.equals(long.class)) {
			throw new WerkConfigException(String.format(
					"Class [%s] Method [%s] is annotated as @JobInit, "
							+ "but its Parameter [%s] type \"int\" or \"long\" is not allowed. "
							+ "Please use boxed type \"Integer\" or \"Long\"",
					clazz, method, param.getName(), paramClass.toString()));
		} else if (!paramClass.equals(Integer.class) && !paramClass.equals(Long.class)) {
			throw new WerkConfigException(String.format(
					"Class [%s] Method [%s] is annotated as @JobInit, "
							+ "and its Parameter [%s] annotated as @DefaultLongParameter "
							+ "should be of boxed type \"Integer\" or \"Long\"",
					clazz, method, param.getName(), paramClass.toString()));
		}

		String name = rangeLongParameter.name() == null ? null : rangeLongParameter.name().trim();
		if ((name == null) || (name.equals("")))
			name = param.getName();
		ParameterType type = ParameterType.LONG;
		boolean isOptional = rangeLongParameter.isOptional();
		String description = rangeLongParameter.description();
		
		org.werk.processing.parameters.Parameter start = new LongParameterImpl(rangeLongParameter.start());
		org.werk.processing.parameters.Parameter end = new LongParameterImpl(rangeLongParameter.end());
		boolean startInclusive = rangeLongParameter.startInclusive();
		boolean endInclusive = rangeLongParameter.endInclusive();
		boolean prohibitRange = rangeLongParameter.prohibitRange();

		return new RangeJobInputParameterImpl(name, type, isOptional, description, 
				start, end, startInclusive, endInclusive, prohibitRange);
	}

	@SuppressWarnings({ "rawtypes" })
	protected RangeJobInputParameter loadRangeDoubleParameter(Parameter param, String clazz,
			String method, Class paramClass, RangeDoubleParameter rangeDoubleParameter) throws WerkConfigException {
		if (paramClass.equals(double.class)) {
			throw new WerkConfigException(String.format("Class [%s] Method [%s] is annotated as @JobInit, "
					+ "but its Parameter [%s] type \"double\" is not allowed. " + "Please use boxed type \"Double\"",
					clazz, method, param.getName(), paramClass.toString()));
		} else if (!paramClass.equals(Double.class)) {
			throw new WerkConfigException(String.format(
					"Class [%s] Method [%s] is annotated as @JobInit, "
							+ "and its Parameter [%s] annotated as @DefaultDoubleParameter "
							+ "should be of boxed type \"Double\"",
					clazz, method, param.getName(), paramClass.toString()));
		}

		String name = rangeDoubleParameter.name() == null ? null : rangeDoubleParameter.name().trim();
		if ((name == null) || (name.equals("")))
			name = param.getName();
		ParameterType type = ParameterType.DOUBLE;
		boolean isOptional = rangeDoubleParameter.isOptional();
		String description = rangeDoubleParameter.description();
		
		org.werk.processing.parameters.Parameter start = new DoubleParameterImpl(rangeDoubleParameter.start());
		org.werk.processing.parameters.Parameter end = new DoubleParameterImpl(rangeDoubleParameter.end());
		boolean startInclusive = rangeDoubleParameter.startInclusive();
		boolean endInclusive = rangeDoubleParameter.endInclusive();
		boolean prohibitRange = rangeDoubleParameter.prohibitRange();

		return new RangeJobInputParameterImpl(name, type, isOptional, description, 
				start, end, startInclusive, endInclusive, prohibitRange);
	}

	@SuppressWarnings({ "rawtypes" })
	protected RangeJobInputParameter loadRangeStringParameter(Parameter param, String clazz,
			String method, Class paramClass, RangeStringParameter rangeStringParameter) throws WerkConfigException {
		if (!paramClass.equals(String.class)) {
			throw new WerkConfigException(String.format("Class [%s] Method [%s] is annotated as @JobInit, "
					+ "and its Parameter [%s] annotated as @DefaultStringParameter " + "should be of type \"String\"",
					clazz, method, param.getName(), paramClass.toString()));
		}

		String name = rangeStringParameter.name() == null ? null : rangeStringParameter.name().trim();
		if ((name == null) || (name.equals("")))
			name = param.getName();
		ParameterType type = ParameterType.STRING;
		boolean isOptional = rangeStringParameter.isOptional();
		String description = rangeStringParameter.description();
		
		org.werk.processing.parameters.Parameter start = new StringParameterImpl(rangeStringParameter.start());
		org.werk.processing.parameters.Parameter end = new StringParameterImpl(rangeStringParameter.end());
		boolean startInclusive = rangeStringParameter.startInclusive();
		boolean endInclusive = rangeStringParameter.endInclusive();
		boolean prohibitRange = rangeStringParameter.prohibitRange();

		return new RangeJobInputParameterImpl(name, type, isOptional, description, 
				start, end, startInclusive, endInclusive, prohibitRange);
	}
	
	//------------ENUMS------------
	
	@SuppressWarnings({ "rawtypes" })
	protected EnumJobInputParameter loadEnumLongParameter(Parameter param, String clazz,
			String method, Class paramClass, EnumLongParameter enumLongParameter) throws WerkConfigException {
		if (paramClass.equals(int.class) || paramClass.equals(long.class)) {
			throw new WerkConfigException(String.format(
					"Class [%s] Method [%s] is annotated as @JobInit, "
							+ "but its Parameter [%s] type \"int\" or \"long\" is not allowed. "
							+ "Please use boxed type \"Integer\" or \"Long\"",
					clazz, method, param.getName(), paramClass.toString()));
		} else if (!paramClass.equals(Integer.class) && !paramClass.equals(Long.class)) {
			throw new WerkConfigException(String.format(
					"Class [%s] Method [%s] is annotated as @JobInit, "
							+ "and its Parameter [%s] annotated as @DefaultLongParameter "
							+ "should be of boxed type \"Integer\" or \"Long\"",
					clazz, method, param.getName(), paramClass.toString()));
		}

		String name = enumLongParameter.name() == null ? null : enumLongParameter.name().trim();
		if ((name == null) || (name.equals("")))
			name = param.getName();
		ParameterType type = ParameterType.LONG;
		boolean isOptional = enumLongParameter.isOptional();
		String description = enumLongParameter.description();
		
		List<org.werk.processing.parameters.Parameter> values = 
				new ArrayList<org.werk.processing.parameters.Parameter>();
		for (long value : enumLongParameter.values())
			values.add(new LongParameterImpl(value));
		
		boolean prohibitValues = enumLongParameter.prohibitValues();

		return new EnumJobInputParameterImpl(name, type, isOptional, description, values, prohibitValues);
	}

	@SuppressWarnings({ "rawtypes" })
	protected EnumJobInputParameter loadEnumDoubleParameter(Parameter param, String clazz,
			String method, Class paramClass, EnumDoubleParameter enumDoubleParameter) throws WerkConfigException {
		if (paramClass.equals(double.class)) {
			throw new WerkConfigException(String.format("Class [%s] Method [%s] is annotated as @JobInit, "
					+ "but its Parameter [%s] type \"double\" is not allowed. " + "Please use boxed type \"Double\"",
					clazz, method, param.getName(), paramClass.toString()));
		} else if (!paramClass.equals(Double.class)) {
			throw new WerkConfigException(String.format(
					"Class [%s] Method [%s] is annotated as @JobInit, "
							+ "and its Parameter [%s] annotated as @DefaultDoubleParameter "
							+ "should be of boxed type \"Double\"",
					clazz, method, param.getName(), paramClass.toString()));
		}

		String name = enumDoubleParameter.name() == null ? null : enumDoubleParameter.name().trim();
		if ((name == null) || (name.equals("")))
			name = param.getName();
		ParameterType type = ParameterType.DOUBLE;
		boolean isOptional = enumDoubleParameter.isOptional();
		String description = enumDoubleParameter.description();
		
		List<org.werk.processing.parameters.Parameter> values = 
				new ArrayList<org.werk.processing.parameters.Parameter>();
		for (double value : enumDoubleParameter.values())
			values.add(new DoubleParameterImpl(value));
		
		boolean prohibitValues = enumDoubleParameter.prohibitValues();

		return new EnumJobInputParameterImpl(name, type, isOptional, description, 
				values, prohibitValues);
	}

	@SuppressWarnings({ "rawtypes" })
	protected EnumJobInputParameter loadEnumStringParameter(Parameter param, String clazz,
			String method, Class paramClass, EnumStringParameter enumStringParameter) throws WerkConfigException {
		if (!paramClass.equals(String.class)) {
			throw new WerkConfigException(String.format("Class [%s] Method [%s] is annotated as @JobInit, "
					+ "and its Parameter [%s] annotated as @DefaultStringParameter " + "should be of type \"String\"",
					clazz, method, param.getName(), paramClass.toString()));
		}

		String name = enumStringParameter.name() == null ? null : enumStringParameter.name().trim();
		if ((name == null) || (name.equals("")))
			name = param.getName();
		ParameterType type = ParameterType.STRING;
		boolean isOptional = enumStringParameter.isOptional();
		String description = enumStringParameter.description();
		
		List<org.werk.processing.parameters.Parameter> values = 
				new ArrayList<org.werk.processing.parameters.Parameter>();
		for (String value : enumStringParameter.values())
			values.add(new StringParameterImpl(value));
		
		boolean prohibitValues = enumStringParameter.prohibitValues();

		return new EnumJobInputParameterImpl(name, type, isOptional, description, 
				values, prohibitValues);
	}

	//------------COMMON------------
	
	@SuppressWarnings({ "rawtypes" })
	protected org.werk.meta.inputparameters.JobInputParameter loadJobInputParameter(Parameter param, String clazz,
			String method, Class paramClass, JobInputParameter jobInputParameter) throws WerkConfigException {
		ParameterType type;

		if (paramClass.equals(int.class) || paramClass.equals(long.class)) {
			throw new WerkConfigException(String.format(
					"Class [%s] Method [%s] is annotated as @JobInit, "
							+ "but its Parameter [%s] type \"int\" or \"long\" is not allowed. "
							+ "Please use boxed type \"Integer\" or \"Long\"",
					clazz, method, param.getName(), paramClass.toString()));
		} else if (paramClass.equals(double.class)) {
			throw new WerkConfigException(String.format("Class [%s] Method [%s] is annotated as @JobInit, "
					+ "but its Parameter [%s] type \"double\" is not allowed. " + "Please use boxed type \"Double\"",
					clazz, method, param.getName(), paramClass.toString()));
		} else if (paramClass.equals(boolean.class)) {
			throw new WerkConfigException(String.format("Class [%s] Method [%s] is annotated as @JobInit, "
					+ "but its Parameter [%s] type \"boolean\" is not allowed. " + "Please use boxed type \"Boolean\"",
					clazz, method, param.getName(), paramClass.toString()));
		} else if (paramClass.equals(Integer.class) || paramClass.equals(Long.class)) {
			type = ParameterType.LONG;
		} else if (paramClass.equals(Double.class)) {
			type = ParameterType.DOUBLE;
		} else if (paramClass.equals(Boolean.class)) {
			type = ParameterType.BOOL;
		} else if (paramClass.equals(String.class)) {
			type = ParameterType.STRING;
		} else if (paramClass.equals(List.class)) {
			type = ParameterType.LIST;
		} else if (paramClass.equals(Map.class)) {
			type = ParameterType.DICTIONARY;
		} else
			throw new WerkConfigException(String.format("Class [%s] Method [%s] is annotated as @JobInit, "
					+ "but its Parameter [%s] type is not allowed for Werk parameter [%s]. "
					+ "Allowed types: [\"Integer\" \"Long\" \"Double\" \"Boolean\" \"String\" \"List\" \"Map\"]", clazz,
					method, param.getName(), paramClass.toString()));

		String name = jobInputParameter.name() == null ? null : jobInputParameter.name().trim();
		if ((name == null) || (name.equals("")))
			name = param.getName();
		boolean isOptional = jobInputParameter.isOptional();
		String description = jobInputParameter.description();

		return new JobInputParameterImpl(name, type, isOptional, description);
	}

	//------------DEFAULT VALUES------------
	
	@SuppressWarnings({ "rawtypes" })
	protected DefaultValueJobInputParameter loadDefaultLongParameter(Parameter param, String clazz,
			String method, Class paramClass, DefaultLongParameter defaultLongParameter) throws WerkConfigException {
		if (paramClass.equals(int.class) || paramClass.equals(long.class)) {
			throw new WerkConfigException(String.format(
					"Class [%s] Method [%s] is annotated as @JobInit, "
							+ "but its Parameter [%s] type \"int\" or \"long\" is not allowed. "
							+ "Please use boxed type \"Integer\" or \"Long\"",
					clazz, method, param.getName(), paramClass.toString()));
		} else if (!paramClass.equals(Integer.class) && !paramClass.equals(Long.class)) {
			throw new WerkConfigException(String.format(
					"Class [%s] Method [%s] is annotated as @JobInit, "
							+ "and its Parameter [%s] annotated as @DefaultLongParameter "
							+ "should be of boxed type \"Integer\" or \"Long\"",
					clazz, method, param.getName(), paramClass.toString()));
		}

		String name = defaultLongParameter.name() == null ? null : defaultLongParameter.name().trim();
		if ((name == null) || (name.equals("")))
			name = param.getName();
		ParameterType type = ParameterType.LONG;
		boolean isOptional = false;
		String description = defaultLongParameter.description();
		boolean isDefaultValueImmutable = defaultLongParameter.isDefaultValueImmutable();
		LongParameter defaultValue = new LongParameterImpl(defaultLongParameter.defaultValue());

		return new DefaultValueJobInputParameterImpl(name, type, isOptional, description, isDefaultValueImmutable,
				defaultValue);
	}

	@SuppressWarnings({ "rawtypes" })
	protected DefaultValueJobInputParameter loadDefaultDoubleParameter(Parameter param, String clazz,
			String method, Class paramClass, DefaultDoubleParameter defaultDoubleParameter) throws WerkConfigException {
		if (paramClass.equals(double.class)) {
			throw new WerkConfigException(String.format("Class [%s] Method [%s] is annotated as @JobInit, "
					+ "but its Parameter [%s] type \"double\" is not allowed. " + "Please use boxed type \"Double\"",
					clazz, method, param.getName(), paramClass.toString()));
		} else if (!paramClass.equals(Double.class)) {
			throw new WerkConfigException(String.format(
					"Class [%s] Method [%s] is annotated as @JobInit, "
							+ "and its Parameter [%s] annotated as @DefaultDoubleParameter "
							+ "should be of boxed type \"Double\"",
					clazz, method, param.getName(), paramClass.toString()));
		}

		String name = defaultDoubleParameter.name() == null ? null : defaultDoubleParameter.name().trim();
		if ((name == null) || (name.equals("")))
			name = param.getName();
		ParameterType type = ParameterType.DOUBLE;
		boolean isOptional = false;
		String description = defaultDoubleParameter.description();
		boolean isDefaultValueImmutable = defaultDoubleParameter.isDefaultValueImmutable();
		DoubleParameter defaultValue = new DoubleParameterImpl(defaultDoubleParameter.defaultValue());

		return new DefaultValueJobInputParameterImpl(name, type, isOptional, description, isDefaultValueImmutable,
				defaultValue);
	}

	@SuppressWarnings({ "rawtypes" })
	protected DefaultValueJobInputParameter loadDefaultBoolParameter(Parameter param, String clazz,
			String method, Class paramClass, DefaultBoolParameter defaultBoolParameter) throws WerkConfigException {
		if (paramClass.equals(boolean.class)) {
			throw new WerkConfigException(String.format("Class [%s] Method [%s] is annotated as @JobInit, "
					+ "but its Parameter [%s] type \"boolean\" is not allowed. " + "Please use boxed type \"Boolean\"",
					clazz, method, param.getName(), paramClass.toString()));
		} else if (!paramClass.equals(Boolean.class)) {
			throw new WerkConfigException(String.format(
					"Class [%s] Method [%s] is annotated as @JobInit, "
							+ "and its Parameter [%s] annotated as @DefaultBoolParameter "
							+ "should be of boxed type \"Boolean\"",
					clazz, method, param.getName(), paramClass.toString()));
		}

		String name = defaultBoolParameter.name() == null ? null : defaultBoolParameter.name().trim();
		if ((name == null) || (name.equals("")))
			name = param.getName();
		ParameterType type = ParameterType.BOOL;
		boolean isOptional = false;
		String description = defaultBoolParameter.description();
		boolean isDefaultValueImmutable = defaultBoolParameter.isDefaultValueImmutable();
		BoolParameter defaultValue = new BoolParameterImpl(defaultBoolParameter.defaultValue());

		return new DefaultValueJobInputParameterImpl(name, type, isOptional, description, isDefaultValueImmutable,
				defaultValue);
	}

	@SuppressWarnings({ "rawtypes" })
	protected DefaultValueJobInputParameter loadDefaultStringParameter(Parameter param, String clazz,
			String method, Class paramClass, DefaultStringParameter defaultStringParameter) throws WerkConfigException {
		if (!paramClass.equals(String.class)) {
			throw new WerkConfigException(String.format("Class [%s] Method [%s] is annotated as @JobInit, "
					+ "and its Parameter [%s] annotated as @DefaultStringParameter " + "should be of type \"String\"",
					clazz, method, param.getName(), paramClass.toString()));
		}

		String name = defaultStringParameter.name() == null ? null : defaultStringParameter.name().trim();
		if ((name == null) || (name.equals("")))
			name = param.getName();
		ParameterType type = ParameterType.STRING;
		boolean isOptional = false;
		String description = defaultStringParameter.description();
		boolean isDefaultValueImmutable = defaultStringParameter.isDefaultValueImmutable();
		StringParameter defaultValue = new StringParameterImpl(defaultStringParameter.defaultValue());

		return new DefaultValueJobInputParameterImpl(name, type, isOptional, description, isDefaultValueImmutable,
				defaultValue);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected DefaultValueJobInputParameter loadDefaultListParameter(Parameter param, String clazz,
			String method, Class paramClass, DefaultListParameter defaultListParameter)
			throws WerkConfigException, NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		if (!paramClass.equals(List.class)) {
			throw new WerkConfigException(String.format("Class [%s] Method [%s] is annotated as @JobInit, "
					+ "and its Parameter [%s] annotated as @DefaultListParameter " + "should be of type \"List\"",
					clazz, method, param.getName(), paramClass.toString()));
		}

		String name = defaultListParameter.name() == null ? null : defaultListParameter.name().trim();
		if ((name == null) || (name.equals("")))
			name = param.getName();
		ParameterType type = ParameterType.LIST;
		boolean isOptional = false;
		String description = defaultListParameter.description();
		boolean isDefaultValueImmutable = defaultListParameter.isDefaultValueImmutable();

		Method listGetter = paramClass.getMethod(defaultListParameter.listValueGetterMethod());
		if (listGetter == null) {
			throw new WerkConfigException(String.format(
					"Class [%s] Method [%s] annotated as @JobInit, "
							+ "Parameter [%s] refers to nonexistent ListValueGetter method [%s]",
					clazz, method, param.getName(), defaultListParameter.listValueGetterMethod()));
		}

		List<org.werk.processing.parameters.Parameter> list = (List<org.werk.processing.parameters.Parameter>) listGetter
				.invoke(null);
		ListParameter defaultValue = new ListParameterImpl(list);

		return new DefaultValueJobInputParameterImpl(name, type, isOptional, description, isDefaultValueImmutable,
				defaultValue);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected DefaultValueJobInputParameter loadDefaultDictionaryParameter(Parameter param, String clazz,
			String method, Class paramClass, DefaultDictionaryParameter defaultDictionaryParameter)
			throws WerkConfigException, NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		if (!paramClass.equals(Map.class)) {
			throw new WerkConfigException(String.format("Class [%s] Method [%s] is annotated as @JobInit, "
					+ "and its Parameter [%s] annotated as @DefaultDictionaryParameter " + "should be of type \"Map\"",
					clazz, method, param.getName(), paramClass.toString()));
		}

		String name = defaultDictionaryParameter.name() == null ? null : defaultDictionaryParameter.name().trim();
		if ((name == null) || (name.equals("")))
			name = param.getName();
		ParameterType type = ParameterType.DICTIONARY;
		boolean isOptional = false;
		String description = defaultDictionaryParameter.description();
		boolean isDefaultValueImmutable = defaultDictionaryParameter.isDefaultValueImmutable();

		Method dictGetter = paramClass.getMethod(defaultDictionaryParameter.dictionaryValueGetterMethod());
		if (dictGetter == null) {
			throw new WerkConfigException(String.format(
					"Class [%s] Method [%s] annotated as @JobInit, "
							+ "Parameter [%s] refers to nonexistent DictionaryValueGetter method [%s]",
					clazz, method, param.getName(), defaultDictionaryParameter.dictionaryValueGetterMethod()));
		}

		Map<String, org.werk.processing.parameters.Parameter> dictionary = 
				(Map<String, org.werk.processing.parameters.Parameter>) dictGetter.invoke(null);
		DictionaryParameter defaultValue = new DictionaryParameterImpl(dictionary);

		return new DefaultValueJobInputParameterImpl(name, type, isOptional, description, isDefaultValueImmutable,
				defaultValue);
	}
}
