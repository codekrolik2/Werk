package org.werk.config.annotations;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.scannotation.AnnotationDB;
import org.werk.config.JobTypeImpl;
import org.werk.config.StepExecFactoryImpl;
import org.werk.config.StepTransitionerFactoryImpl;
import org.werk.config.StepTypeImpl;
import org.werk.config.WerkConfig;
import org.werk.config.WerkConfigImpl;
import org.werk.config.WerkConfigLoader;
import org.werk.config.annotations.inject.RollbackTransition;
import org.werk.config.annotations.inject.Transition;
import org.werk.config.annotations.inputparameters.DefaultBoolParameter;
import org.werk.config.annotations.inputparameters.DefaultDictionaryParameter;
import org.werk.config.annotations.inputparameters.DefaultDoubleParameter;
import org.werk.config.annotations.inputparameters.DefaultListParameter;
import org.werk.config.annotations.inputparameters.DefaultLongParameter;
import org.werk.config.annotations.inputparameters.DefaultStringParameter;
import org.werk.config.annotations.inputparameters.JobInputParameter;
import org.werk.exceptions.WerkConfigException;
import org.werk.meta.JobType;
import org.werk.meta.StepExecFactory;
import org.werk.meta.StepTransitionerFactory;
import org.werk.meta.inputparameters.impl.DefaultValueJobInputParameterImpl;
import org.werk.meta.inputparameters.impl.JobInputParameterImpl;
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

public class AnnotationsWerkConfigLoader<J> implements WerkConfigLoader<J> {
	@SuppressWarnings("deprecation")
	static List<URL> findClassPaths() throws MalformedURLException {
		List<URL> list = new ArrayList<>();
		String classpath = System.getProperty("java.class.path");
		StringTokenizer tokenizer = new StringTokenizer(classpath, File.pathSeparator);
		
		while (tokenizer.hasMoreTokens()) {
			String path = tokenizer.nextToken();
			File fp = new File(path);
			if (!fp.exists()) {
				System.out.println("File in java.class.path doesn't exist: " + fp);
			} else {
				list.add(fp.toURL());
			}
		}
		
		return list;
	}
	
	@Override
	public WerkConfig<J> loadWerkConfig() throws WerkConfigException {
		try {
			URL[] urls = findClassPaths().toArray(new URL[] {});
			AnnotationDB db = new AnnotationDB();
			db.scanArchives(urls);
			
			WerkConfigImpl<J> config = new WerkConfigImpl<J>();
			
			Set<String> jobClassesSet = db.getAnnotationIndex().get(JobType.class.getName());
			for (String className : jobClassesSet) {
				JobType jobTypeObj = loadJobType(className);
				config.addJobType(jobTypeObj);
			}
			
			Set<String> stepsClassesSet = db.getAnnotationIndex().get(StepType.class.getName());
			for (String className : stepsClassesSet) {
				org.werk.meta.StepType<J> stepTypeObj = loadStepType(className);
				config.addStepType(stepTypeObj);
			}
			
			Set<String> stepsClassesSetF = db.getAnnotationIndex().get(StepTypeFactories.class.getName());
			for (String className : stepsClassesSetF) {
				org.werk.meta.StepType<J> stepTypeObj = loadStepTypeF(className);
				config.addStepType(stepTypeObj);
			}
			
			return config;
		} catch (ClassNotFoundException | IOException | NoSuchMethodException | SecurityException | 
				IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException e) {
			throw new WerkConfigException(e);
		}
	}

	public static void main(String[] args) throws Exception {
		new AnnotationsWerkConfigLoader<Long>().loadWerkConfig();
	}
	
	
	protected JobType loadJobType(String className) throws NoSuchMethodException, SecurityException, IllegalAccessException, 
			IllegalArgumentException, InvocationTargetException, WerkConfigException, ClassNotFoundException {
		@SuppressWarnings("rawtypes")
		Class classObj = Class.forName(className);
		@SuppressWarnings("unchecked")
		JobType jobType = (JobType)classObj.getAnnotation(JobType.class);
		
		String jobTypeName = jobType.getJobTypeName();
		String firstStepTypeName = jobType.getFirstStepTypeName();
		List<String> stepTypes = jobType.getStepTypes();
		String description = jobType.getDescription();
		String customInfo = jobType.getCustomInfo();
		boolean forceAcyclic = jobType.isForceAcyclic();
		long version = jobType.getVersion();
		
		List<List<org.werk.meta.inputparameters.JobInputParameter>> initInfo = new ArrayList<>();
		Method[] methods = classObj.getDeclaredMethods();
		for (Method method : methods) {
			JobInit jobInit = null;
			try {
				jobInit = (JobInit)method.getAnnotation(JobInit.class);
			} catch(NullPointerException npe) { }
			
			if (jobInit != null) {
				List<org.werk.meta.inputparameters.JobInputParameter> methodParams = new ArrayList<>(); 
				for (Parameter param : method.getParameters())
					methodParams.add(loadInputParameter(param, className, method.getName()));
				initInfo.add(methodParams);
			}
		}
		
		return new JobTypeImpl(jobTypeName, stepTypes, initInfo, firstStepTypeName, description, customInfo, forceAcyclic, version);
	}
	
	protected List<String> loadAllowedTransitions(@SuppressWarnings("rawtypes") Class classObj) throws WerkConfigException {
		List<String> allowedTransitions = new ArrayList<>();
		for (Field field : classObj.getDeclaredFields()) {
			Transition t = null;
			try {
				t = (Transition)field.getAnnotation(Transition.class);
			} catch(NullPointerException e) {}
			
			if (!field.getType().equals(String.class))
				throw new WerkConfigException(
						String.format("Class [%s] Field [%s] is annotated as @Transition, must be of type String, but is [%s]", 
								classObj.toString(), field.toString(), field.getType().toString())
					);
			
			String name = t.name() == null ? null : t.name().trim();
			if ((name == null) || (name.equals("")))
				name = field.getName();
			
			allowedTransitions.add(name);
		}
		
		return allowedTransitions;
	}
	
	protected List<String> loadAllowedRollbackTransitions(@SuppressWarnings("rawtypes") Class classObj) throws WerkConfigException {
		List<String> allowedRollbackTransitions = new ArrayList<>();
		for (Field field : classObj.getDeclaredFields()) {
			RollbackTransition t = null;
			try {
				t = (RollbackTransition)field.getAnnotation(RollbackTransition.class);
			} catch(NullPointerException e) {}
			
			if (!field.getType().equals(String.class))
				throw new WerkConfigException(
						String.format("Class [%s] Field [%s] is annotated as @RollbackTransition, must be of type String, but is [%s]", 
								classObj.toString(), field.toString(), field.getType().toString())
					);
			
			String name = t.name() == null ? null : t.name().trim();
			if ((name == null) || (name.equals("")))
				name = field.getName();
			
			allowedRollbackTransitions.add(name);
		}
		
		return allowedRollbackTransitions;
	}
	
	protected org.werk.meta.StepType<J> loadStepType(String className) throws ClassNotFoundException, WerkConfigException {
		@SuppressWarnings("rawtypes")
		Class classObj = Class.forName(className);
		
		@SuppressWarnings("unchecked")
		StepType stepType = (StepType)classObj.getAnnotation(StepType.class);
		
		String stepTypeName = stepType.name();
		String processingDescription = stepType.processingDescription();
		String rollbackDescription = stepType.rollbackDescription();
		
		List<String> allowedTransitions = loadAllowedTransitions(classObj);
		List<String> allowedRollbackTransitions = loadAllowedRollbackTransitions(classObj);
		String execConfig = stepType.execConfig();
		String transitionerConfig = stepType.transitionerConfig();

		StepExecFactory<J> stepExecFactory = new StepExecFactoryImpl<J>(stepType.stepExecClass());
		StepTransitionerFactory<J> stepTransitionerFactory = new StepTransitionerFactoryImpl<J>(stepType.stepTransitionerClass());
		
		return new StepTypeImpl<J>(stepTypeName, allowedTransitions, allowedRollbackTransitions, 
				stepExecFactory, stepTransitionerFactory, processingDescription, rollbackDescription, execConfig, transitionerConfig);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected org.werk.meta.StepType<J> loadStepTypeF(String className) throws ClassNotFoundException, 
			NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, 
			IllegalArgumentException, InvocationTargetException, WerkConfigException {
		Class classObj = Class.forName(className);
		StepTypeFactories stepType = (StepTypeFactories)classObj.getAnnotation(StepTypeFactories.class);
		
		String stepTypeName = stepType.name();
		String processingDescription = stepType.processingDescription();
		String rollbackDescription = stepType.rollbackDescription();
		
		List<String> allowedTransitions = loadAllowedTransitions(classObj);
		List<String> allowedRollbackTransitions = loadAllowedRollbackTransitions(classObj);
		String execConfig = stepType.execConfig();
		String transitionerConfig = stepType.transitionerConfig();
		
		Class<StepExecFactory> stepExecClass = stepType.stepExecFactoryClass();
		Constructor<StepExecFactory> stepExecConstr = stepExecClass.getConstructor();
		StepExecFactory<J> stepExecFactory = (StepExecFactory<J>)stepExecConstr.newInstance();
		
		Class<StepTransitionerFactory> stepTransitionerClass = stepType.stepTransitionerFactoryClass();
		Constructor<StepTransitionerFactory> stepTransitionerConstr = stepTransitionerClass.getConstructor();
		StepTransitionerFactory<J> stepTransitionerFactory = (StepTransitionerFactory<J>)stepTransitionerConstr.newInstance();
		
		return new StepTypeImpl<J>(stepTypeName, allowedTransitions, allowedRollbackTransitions, stepExecFactory, 
				stepTransitionerFactory, processingDescription, rollbackDescription, execConfig, transitionerConfig);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected org.werk.meta.inputparameters.JobInputParameter 
		loadInputParameter(Parameter param, String clazz, String method) throws WerkConfigException, 
			NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, 
			InvocationTargetException {
		Class paramClass = param.getType();
		
		{
			JobInputParameter jobInputParameter = null;
			try {
				jobInputParameter = (JobInputParameter)param.getAnnotation(JobInputParameter.class);
			} catch (NullPointerException npe) {}
			if (jobInputParameter != null) {
				ParameterType type;
				
				if (paramClass.equals(Integer.class) || paramClass.equals(Long.class)) {
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
				} else if (paramClass.equals(int.class) || paramClass.equals(long.class)) {
					throw new WerkConfigException(
						String.format("Class [%s] Method [%s] is annotated as @JobInit, " + 
								"but its Parameter [%s] type \"int\" or \"long\" is not allowed. " + 
								"Please use boxed type \"Integer\" or \"Long\"", 
								clazz, method, param.getName(), paramClass.toString())
					);
				} else if (paramClass.equals(double.class)) {
					throw new WerkConfigException(
							String.format("Class [%s] Method [%s] is annotated as @JobInit, " + 
									"but its Parameter [%s] type \"double\" is not allowed. " + 
									"Please use boxed type \"Double\"", 
									clazz, method, param.getName(), paramClass.toString())
						);
				} else if (paramClass.equals(boolean.class)) {
					throw new WerkConfigException(
							String.format("Class [%s] Method [%s] is annotated as @JobInit, " + 
									"but its Parameter [%s] type \"double\" is not allowed. " + 
									"Please use boxed type \"Double\"", 
									clazz, method, param.getName(), paramClass.toString())
						);
				} else throw new WerkConfigException(
					String.format("Class [%s] Method [%s] is annotated as @JobInit, " + 
							"but its Parameter [%s] type is not allowed for Werk parameter [%s]. " + 
							"Allowed types: [\"Integer\" \"Long\" \"Double\" \"Boolean\" \"String\" \"List\" \"Map\"]", 
							clazz, method, param.getName(), paramClass.toString())
				);
				
				String name = jobInputParameter.name() == null ? null : jobInputParameter.name().trim();
				if ((name == null) || (name.equals("")))
					name = param.getName();
				boolean isOptional = jobInputParameter.isOptional();
				String description = jobInputParameter.description();
				
				return new JobInputParameterImpl(name, type, isOptional, description);
			}
		}
		
		{
			DefaultLongParameter defaultLongParameter = null;
			try {
				defaultLongParameter = (DefaultLongParameter)param.getAnnotation(DefaultLongParameter.class);
			} catch (NullPointerException e) {}
			if (defaultLongParameter != null) {
				String name = defaultLongParameter.name() == null ? null : defaultLongParameter.name().trim();
				if ((name == null) || (name.equals("")))
					name = param.getName();
				ParameterType type = ParameterType.LONG;
				boolean isOptional = defaultLongParameter.isOptional();
				String description = defaultLongParameter.description();
				boolean isDefaultValueImmutable = defaultLongParameter.isDefaultValueImmutable();
				LongParameter defaultValue = new LongParameterImpl(defaultLongParameter.defaultValue());
				
				return new DefaultValueJobInputParameterImpl(name, type, isOptional, description, 
						isDefaultValueImmutable, defaultValue);
			}
		}
		
		{
			DefaultDoubleParameter defaultDoubleParameter = null;
			try {
				defaultDoubleParameter = (DefaultDoubleParameter)param.getAnnotation(DefaultDoubleParameter.class);
			} catch (NullPointerException e) {}
			if (defaultDoubleParameter != null) {
				String name = defaultDoubleParameter.name() == null ? null : defaultDoubleParameter.name().trim();
				if ((name == null) || (name.equals("")))
					name = param.getName();
				ParameterType type = ParameterType.DOUBLE;
				boolean isOptional = defaultDoubleParameter.isOptional();
				String description = defaultDoubleParameter.description();
				boolean isDefaultValueImmutable = defaultDoubleParameter.isDefaultValueImmutable();
				DoubleParameter defaultValue = new DoubleParameterImpl(defaultDoubleParameter.defaultValue());
				
				return new DefaultValueJobInputParameterImpl(name, type, isOptional, description, 
						isDefaultValueImmutable, defaultValue);
			}
		}
		
		{
			DefaultBoolParameter defaultBoolParameter = null;
			try {
				defaultBoolParameter = (DefaultBoolParameter)param.getAnnotation(DefaultBoolParameter.class);
			} catch (NullPointerException e) {}
			if (defaultBoolParameter != null) {
				String name = defaultBoolParameter.name() == null ? null : defaultBoolParameter.name().trim();
				if ((name == null) || (name.equals("")))
					name = param.getName();
				ParameterType type = ParameterType.BOOL;
				boolean isOptional = defaultBoolParameter.isOptional();
				String description = defaultBoolParameter.description();
				boolean isDefaultValueImmutable = defaultBoolParameter.isDefaultValueImmutable();
				BoolParameter defaultValue = new BoolParameterImpl(defaultBoolParameter.defaultValue());
				
				return new DefaultValueJobInputParameterImpl(name, type, isOptional, description, 
						isDefaultValueImmutable, defaultValue);
			}
		}
		
		{
			DefaultStringParameter defaultStringParameter = null;
			try {
				defaultStringParameter = (DefaultStringParameter)param.getAnnotation(DefaultStringParameter.class);
			} catch (NullPointerException npe) {}
			if (defaultStringParameter != null) {
				String name = defaultStringParameter.name() == null ? null : defaultStringParameter.name().trim();
				if ((name == null) || (name.equals("")))
					name = param.getName();
				ParameterType type = ParameterType.STRING;
				boolean isOptional = defaultStringParameter.isOptional();
				String description = defaultStringParameter.description();
				boolean isDefaultValueImmutable = defaultStringParameter.isDefaultValueImmutable();
				StringParameter defaultValue = new StringParameterImpl(defaultStringParameter.defaultValue());
				
				return new DefaultValueJobInputParameterImpl(name, type, isOptional, description, 
						isDefaultValueImmutable, defaultValue);
			}
		}
		
		{
			DefaultListParameter defaultListParameter = null;
			try {
				defaultListParameter = (DefaultListParameter)param.getAnnotation(DefaultListParameter.class);
			} catch (NullPointerException npe) {}
			if (defaultListParameter != null) {
				String name = defaultListParameter.name() == null ? null : defaultListParameter.name().trim();
				if ((name == null) || (name.equals("")))
					name = param.getName();
				ParameterType type = ParameterType.LIST;
				boolean isOptional = defaultListParameter.isOptional();
				String description = defaultListParameter.description();
				boolean isDefaultValueImmutable = defaultListParameter.isDefaultValueImmutable();
				
				Method listGetter = paramClass.getMethod(defaultListParameter.listValueGetterMethod());
				if (listGetter == null) {
					throw new WerkConfigException(
							String.format("Class [%s] Method [%s] annotated as @JobInit, " + 
									"Parameter [%s] refers to nonexistent ListValueGetter method [%s]", 
									clazz, method, param.getName(), 
									defaultListParameter.listValueGetterMethod())
						);					
				}
				
				List<org.werk.processing.parameters.Parameter> list = 
						(List<org.werk.processing.parameters.Parameter>)listGetter.invoke(null);
				ListParameter defaultValue = new ListParameterImpl(list);
				
				return new DefaultValueJobInputParameterImpl(name, type, isOptional, description, 
						isDefaultValueImmutable, defaultValue);
			}
		}
		
		{
			DefaultDictionaryParameter defaultDictionaryParameter = null;
			try {
				defaultDictionaryParameter = (DefaultDictionaryParameter)param.getAnnotation(DefaultDictionaryParameter.class);
			} catch (NullPointerException npe) {}
			if (defaultDictionaryParameter != null) {
				String name = defaultDictionaryParameter.name() == null ? null : defaultDictionaryParameter.name().trim();
				if ((name == null) || (name.equals("")))
					name = param.getName();
				ParameterType type = ParameterType.DICTIONARY;
				boolean isOptional = defaultDictionaryParameter.isOptional();
				String description = defaultDictionaryParameter.description();
				boolean isDefaultValueImmutable = defaultDictionaryParameter.isDefaultValueImmutable();
				
				Method dictGetter = paramClass.getMethod(defaultDictionaryParameter.dictionaryValueGetterMethod());
				if (dictGetter == null) {
					throw new WerkConfigException(
							String.format("Class [%s] Method [%s] annotated as @JobInit, " + 
									"Parameter [%s] refers to nonexistent DictionaryValueGetter method [%s]", 
									clazz, method, param.getName(), 
									defaultDictionaryParameter.dictionaryValueGetterMethod())
						);					
				}
				
				Map<String, org.werk.processing.parameters.Parameter> dictionary = 
						(Map<String, org.werk.processing.parameters.Parameter>)dictGetter.invoke(null);
				DictionaryParameter defaultValue = new DictionaryParameterImpl(dictionary);
				
				return new DefaultValueJobInputParameterImpl(name, type, isOptional, description, 
						isDefaultValueImmutable, defaultValue);
			}
		}
		
		throw new WerkConfigException(
			String.format("Class [%s] Method [%s] is annotated as @JobInit, but its Parameter #%d is not annotated as a job input parameter ", 
					clazz, method, param.getName())
		);
	}
}
