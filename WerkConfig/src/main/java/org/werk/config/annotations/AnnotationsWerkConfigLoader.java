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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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
import org.werk.exceptions.WerkConfigException;
import org.werk.meta.OverflowAction;
import org.werk.meta.StepExecFactory;
import org.werk.meta.StepTransitionerFactory;

public class AnnotationsWerkConfigLoader<J> implements WerkConfigLoader<J> {
	protected JobInputParameterLoader loader = new JobInputParameterLoader();
	
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
				list.add(fp.toURI().toURL());
			}
		}
		
		return list;
	}
	
	@Override
	public WerkConfig<J> loadWerkConfig() throws WerkConfigException {
		try {
			URL[] urls = findClassPaths().toArray(new URL[] {});
			return loadWerkConfig(urls);
		} catch (IOException e) {
			throw new WerkConfigException(e);
		}
	}
	
	@Override
	public WerkConfig<J> loadWerkConfig(Object[] loadParameters) throws WerkConfigException {
		try {
			URL[] urls = (URL[])loadParameters;
			AnnotationDB db = new AnnotationDB();
			db.scanArchives(urls);
			
			WerkConfigImpl<J> config = new WerkConfigImpl<J>();
			
			Set<String> jobClassesSet = db.getAnnotationIndex().get(JobType.class.getName());
			if (jobClassesSet != null)
			for (String className : jobClassesSet) {
				org.werk.meta.JobType jobTypeObj = loadJobType(className);
				config.addJobType(jobTypeObj);
			}
			
			Set<String> stepsClassesSet = db.getAnnotationIndex().get(StepType.class.getName());
			if (stepsClassesSet != null)
			for (String className : stepsClassesSet) {
				org.werk.meta.StepType<J> stepTypeObj = loadStepType(className);
				config.addStepType(stepTypeObj);
			}
			
			Set<String> stepsClassesSetF = db.getAnnotationIndex().get(StepTypeFactories.class.getName());
			if (stepsClassesSetF != null)
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
	
	protected org.werk.meta.JobType loadJobType(String className) throws NoSuchMethodException, SecurityException, IllegalAccessException, 
			IllegalArgumentException, InvocationTargetException, WerkConfigException, ClassNotFoundException {
		@SuppressWarnings("rawtypes")
		Class classObj = Class.forName(className);
		@SuppressWarnings("unchecked")
		JobType jobType = (JobType)classObj.getAnnotation(JobType.class);
		
		String jobTypeName = jobType.name();
		String firstStepTypeName = jobType.firstStepTypeName();
		List<String> stepTypes = Arrays.asList(jobType.stepTypeNames());
		String description = jobType.description();
		String jobConfig = jobType.jobConfig();
		boolean forceAcyclic = jobType.forceAcyclic();
		long version = jobType.version();
		long historyLimit = jobType.historyLimit();
		OverflowAction historyOverflowAction = jobType.historyOverflowAction();
		
		Map<String, List<org.werk.meta.inputparameters.JobInputParameter>> initInfo = new HashMap<>();
		Method[] methods = classObj.getDeclaredMethods();
		for (Method method : methods) {
			JobInit jobInit = null;
			try {
				jobInit = (JobInit)method.getAnnotation(JobInit.class);
			} catch(NullPointerException npe) { }
			
			if (jobInit != null) {
				List<org.werk.meta.inputparameters.JobInputParameter> methodParams = new ArrayList<>(); 
				for (Parameter param : method.getParameters())
					methodParams.add(loader.loadInputParameter(param, className, method.getName()));
				initInfo.put(jobInit.parameterSetName(), methodParams);
			}
		}
		
		return new JobTypeImpl(jobTypeName, stepTypes, initInfo, firstStepTypeName, description, jobConfig, 
				forceAcyclic, version, historyLimit, historyOverflowAction);
	}
	
	protected void checkAllowedTransitions(Set<String> allowedTransitions, @SuppressWarnings("rawtypes") Class classObj) throws WerkConfigException {
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
			
			if (!allowedTransitions.contains(name))
				throw new WerkConfigException(
						String.format("Class [%s] Field [%s] is annotated as @Transition, but the transition is not allowed [%s]", 
								classObj.toString(), field.toString(), name)
					);
		}
	}
	
	protected void checkAllowedRollbackTransitions(Set<String> allowedRollbackTransitions, @SuppressWarnings("rawtypes") Class classObj) throws WerkConfigException {
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
			
			if (!allowedRollbackTransitions.contains(name))
				throw new WerkConfigException(
						String.format("Class [%s] Field [%s] is annotated as @RollbackTransition, but the transition is not allowed [%s]", 
								classObj.toString(), field.toString(), name)
					);
		}
	}
	
	protected org.werk.meta.StepType<J> loadStepType(String className) throws ClassNotFoundException, WerkConfigException {
		@SuppressWarnings("rawtypes")
		Class classObj = Class.forName(className);
		
		@SuppressWarnings("unchecked")
		StepType stepType = (StepType)classObj.getAnnotation(StepType.class);
		
		String stepTypeName = stepType.name();
		String processingDescription = stepType.processingDescription();
		String rollbackDescription = stepType.rollbackDescription();

		Set<String> allowedTransitions = new HashSet<>();
		Set<String> allowedRollbackTransitions = new HashSet<>();
		
		allowedTransitions.addAll(Arrays.asList(stepType.transitions()));
		allowedRollbackTransitions.addAll(Arrays.asList(stepType.rollbackTransitions()));
		
		checkAllowedTransitions(allowedTransitions, classObj);
		checkAllowedRollbackTransitions(allowedRollbackTransitions, classObj);
		
		String execConfig = stepType.execConfig();
		String transitionerConfig = stepType.transitionerConfig();

		@SuppressWarnings("unchecked")
		StepExecFactory<J> stepExecFactory = new StepExecFactoryImpl<J>(stepType.stepExecClass());
		@SuppressWarnings("unchecked")
		StepTransitionerFactory<J> stepTransitionerFactory = new StepTransitionerFactoryImpl<J>(stepType.stepTransitionerClass());
		
		long logLimit = stepType.logLimit();
		OverflowAction logOverflowAction = stepType.logOverflowAction();
		boolean shortTransaction = stepType.shortTransaction();
		
		return new StepTypeImpl<J>(stepTypeName, allowedTransitions, allowedRollbackTransitions, stepExecFactory, 
				stepTransitionerFactory, processingDescription, rollbackDescription, execConfig, transitionerConfig,
				logLimit, logOverflowAction, shortTransaction);
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
		
		Set<String> allowedTransitions = new HashSet<>();
		Set<String> allowedRollbackTransitions = new HashSet<>();
		
		allowedTransitions.addAll(Arrays.asList(stepType.transitions()));
		allowedRollbackTransitions.addAll(Arrays.asList(stepType.rollbackTransitions()));
		
		checkAllowedTransitions(allowedTransitions, classObj);
		checkAllowedRollbackTransitions(allowedRollbackTransitions, classObj);
		
		String execConfig = stepType.execConfig();
		String transitionerConfig = stepType.transitionerConfig();
		
		Class<StepExecFactory> stepExecClass = stepType.stepExecFactoryClass();
		Constructor<StepExecFactory> stepExecConstr = stepExecClass.getConstructor();
		StepExecFactory<J> stepExecFactory = (StepExecFactory<J>)stepExecConstr.newInstance();
		
		Class<StepTransitionerFactory> stepTransitionerClass = stepType.stepTransitionerFactoryClass();
		Constructor<StepTransitionerFactory> stepTransitionerConstr = stepTransitionerClass.getConstructor();
		StepTransitionerFactory<J> stepTransitionerFactory = (StepTransitionerFactory<J>)stepTransitionerConstr.newInstance();
		
		long logLimit = stepType.logLimit();
		OverflowAction logOverflowAction = stepType.logOverflowAction();
		boolean shortTransaction = stepType.shortTransaction();
		
		return new StepTypeImpl<J>(stepTypeName, allowedTransitions, allowedRollbackTransitions, stepExecFactory, 
				stepTransitionerFactory, processingDescription, rollbackDescription, execConfig, transitionerConfig,
				logLimit, logOverflowAction, shortTransaction);
	}
}
