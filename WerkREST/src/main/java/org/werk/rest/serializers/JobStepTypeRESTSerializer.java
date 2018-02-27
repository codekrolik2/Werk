package org.werk.rest.serializers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.werk.meta.JobType;
import org.werk.meta.OverflowAction;
import org.werk.meta.StepExecFactory;
import org.werk.meta.StepTransitionerFactory;
import org.werk.meta.StepType;
import org.werk.meta.inputparameters.DefaultValueJobInputParameter;
import org.werk.meta.inputparameters.EnumJobInputParameter;
import org.werk.meta.inputparameters.JobInputParameter;
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
import org.werk.processing.parameters.Parameter;
import org.werk.processing.parameters.ParameterType;
import org.werk.processing.parameters.StringParameter;
import org.werk.processing.steps.StepExec;
import org.werk.processing.steps.Transitioner;
import org.werk.rest.pojo.RESTJobType;
import org.werk.rest.pojo.RESTStepType;
import org.werk.util.ParameterContextSerializer;

public class JobStepTypeRESTSerializer<J> {
	ParameterContextSerializer parameterContextSerializer;
	
	public JobStepTypeRESTSerializer(ParameterContextSerializer parameterContextSerializer) {
		this.parameterContextSerializer = parameterContextSerializer;
	}
	
	public JobType deserializeJobType(JSONObject jobTypeJSON) {
		String jobTypeName = jobTypeJSON.getString("jobTypeName");
		long version = jobTypeJSON.getLong("version");
		
		String firstStepTypeName = jobTypeJSON.getString("firstStepTypeName");
		
		Set<String> stepTypes = new HashSet<>();
		JSONArray stepTypesArray = jobTypeJSON.getJSONArray("stepTypes");
		for (int i = 0; i < stepTypesArray.length(); i++)
			stepTypes.add(stepTypesArray.getString(i));

		String description = jobTypeJSON.getString("description");
		
		String jobConfig = jobTypeJSON.getString("jobConfig");
		boolean isForceAcyclic = jobTypeJSON.getBoolean("forceAcyclic");

		long historyLimit = jobTypeJSON.getLong("historyLimit");
		OverflowAction historyOverflowAction = OverflowAction.valueOf(jobTypeJSON.getString("historyOverflowAction"));
		
		Map<String, List<JobInputParameter>> initParameters = new HashMap<>();
		JSONObject parameterSets = jobTypeJSON.getJSONObject("initParameters");
		Iterator<?> keys = parameterSets.keys();
		while (keys.hasNext()) {
		    String parameterSetName = (String)keys.next();
		    JSONArray prms = parameterSets.getJSONArray(parameterSetName);
		    
		    List<JobInputParameter> jips = new ArrayList<>();
		    for (int i = 0; i < prms.length(); i++) {
		    	JSONObject jobInputParameterJSON = prms.getJSONObject(i);
		    	jips.add(deserializeJobInputParameter(jobInputParameterJSON));
		    }
		    
		    initParameters.put(parameterSetName, jips);
		}
		
		return new RESTJobType(jobTypeJSON, jobTypeName, stepTypes, initParameters, firstStepTypeName, description,
				jobConfig, isForceAcyclic, version, historyLimit, historyOverflowAction);
	}
	
	public JSONObject serializeJobType(JobType jobType) {
		JSONObject jobTypeJSON = new JSONObject();
		
		jobTypeJSON.put("jobTypeName", jobType.getJobTypeName());
		jobTypeJSON.put("version", jobType.getVersion());
		jobTypeJSON.put("firstStepTypeName", jobType.getFirstStepTypeName());
		
		jobTypeJSON.put("stepTypes", jobType.getStepTypes());
		
		//-------------------------
		
		jobTypeJSON.put("description", jobType.getDescription());
		
		jobTypeJSON.put("jobConfig", jobType.getJobConfig());
		jobTypeJSON.put("forceAcyclic", jobType.isForceAcyclic());
		
		jobTypeJSON.put("historyLimit", jobType.getHistoryLimit());
		jobTypeJSON.put("historyOverflowAction", jobType.getHistoryOverflowAction().toString());
		
		//-------------------------
		
		JSONObject parameterSets = new JSONObject();
		for (Entry<String, List<JobInputParameter>> ent : jobType.getInitParameters().entrySet()) {
			JSONArray prms = new JSONArray();
			for (JobInputParameter jip : ent.getValue())
				prms.put(serializeJobInputParameter(jip));
			
			parameterSets.put(ent.getKey(), prms);
		}
		
		jobTypeJSON.put("initParameters", parameterSets);

		return jobTypeJSON;
	}

	public StepType<J> deserializeStepType(JSONObject stepType) {
		String stepTypeName = stepType.getString("stepTypeName");
		String processingDescription = stepType.getString("processingDescription");
		String rollbackDescription = stepType.getString("rollbackDescription");
		String execConfig = stepType.getString("execConfig");
		String transitionerConfig = stepType.getString("transitionerConfig");
		long logLimit = stepType.getLong("logLimit");
		OverflowAction logOverflowAction = OverflowAction.valueOf(stepType.getString("logOverflowAction"));
		boolean shortTransaction = stepType.getBoolean("shortTransaction");
		
		JSONArray jobTypesJSON = stepType.getJSONArray("jobTypes");
		Set<String> jobTypes = new HashSet<>();
		for (int i = 0; i < jobTypesJSON.length(); i++)
			jobTypes.add(jobTypesJSON.getString(i));
		
		JSONArray allowedTransitionsJSON = stepType.getJSONArray("allowedTransitions");
		Set<String> allowedTransitions = new HashSet<>();
		for (int i = 0; i < allowedTransitionsJSON.length(); i++)
			allowedTransitions.add(allowedTransitionsJSON.getString(i));
		
		JSONArray allowedRollbackTransitionsJSON = stepType.getJSONArray("allowedRollbackTransitions");
		Set<String> allowedRollbackTransitions = new HashSet<>();
		for (int i = 0; i < allowedRollbackTransitionsJSON.length(); i++)
			allowedRollbackTransitions.add(allowedRollbackTransitionsJSON.getString(i));
		
		String stepExecClassName = stepType.getString("stepExec");
		String stepTransitionerName = stepType.getString("transitioner");
		
		StepExecFactory<J> stepExecFactory = new StepExecFactory<J>() {
			@Override public StepExec<J> createStepExec() throws Exception { return null; }
			@SuppressWarnings("rawtypes")
			@Override public Class getStepExecClass() { return null; }
			@Override public String getStepExecClassName() { return stepExecClassName; }
		};
		StepTransitionerFactory<J> stepTransitionerFactory = new StepTransitionerFactory<J>() {
			@Override public Transitioner<J> createStepTransitioner() throws Exception { return null; }
			@SuppressWarnings("rawtypes")
			@Override public Class getTransitionerClass() { return null; }
			@Override public String getTransitionerClassName() { return stepTransitionerName; }
		};
		
		return new RESTStepType<>(stepType, stepTypeName, jobTypes, allowedTransitions, allowedRollbackTransitions, 
				stepExecFactory, stepTransitionerFactory, processingDescription, rollbackDescription, execConfig,
				transitionerConfig, logLimit, logOverflowAction, shortTransaction);
	}
	
	public JSONObject serializeStepType(StepType<?> stepType) {
		JSONObject stepTypeJSON = new JSONObject();
		
		stepTypeJSON.put("stepTypeName", stepType.getStepTypeName());
		
		stepTypeJSON.put("jobTypes", stepType.getJobTypes());
		stepTypeJSON.put("allowedTransitions", stepType.getAllowedTransitions());
		stepTypeJSON.put("allowedRollbackTransitions", stepType.getAllowedRollbackTransitions());
		
		stepTypeJSON.put("stepExec", stepType.getStepExecFactory().getStepExecClass().toString());
		stepTypeJSON.put("transitioner", stepType.getStepTransitionerFactory().getTransitionerClass().toString());

		//-------------------------
		
		stepTypeJSON.put("processingDescription", stepType.getProcessingDescription());
		stepTypeJSON.put("rollbackDescription", stepType.getRollbackDescription());

		stepTypeJSON.put("execConfig", stepType.getExecConfig());
		stepTypeJSON.put("transitionerConfig", stepType.getTransitionerConfig());
		
		stepTypeJSON.put("logLimit", stepType.getLogLimit());
		stepTypeJSON.put("logOverflowAction", stepType.getLogOverflowAction());

		stepTypeJSON.put("shortTransaction", stepType.isShortTransaction());
		
		return stepTypeJSON;
	}
	
	public JobInputParameter deserializeJobInputParameter(JSONObject param) {
		String name = param.getString("name");
		ParameterType type = ParameterType.valueOf(param.getString("type"));
		String description = param.getString("description");
		
		if (param.has("isDefaultValueImmutable")) {
			boolean isDefaultValueImmutable = param.getBoolean("isDefaultValueImmutable");
			Object obj = param.get("defaultValue");
			Parameter defaultValue = parameterContextSerializer.createParameterFromJSONGet(obj);
			
			return new DefaultValueJobInputParameterImpl(name, type, description, 
					isDefaultValueImmutable, defaultValue);
		} else if (param.has("isProhibitValues")) {
			boolean isProhibitValues = param.getBoolean("isProhibitValues");
			
			JSONArray valuesJSON = param.getJSONArray("values");
			List<Parameter> values = new ArrayList<>();
			for (int i = 0; i < valuesJSON.length(); i++) {
				Object obj = valuesJSON.get(i);
				Parameter defaultValue = parameterContextSerializer.createParameterFromJSONGet(obj);
				values.add(defaultValue);
			}
			
			boolean isOptional = param.getBoolean("isOptional");
			return new EnumJobInputParameterImpl(name, type, isOptional, description,
					values, isProhibitValues);
		} else if (param.has("isProhibitRange")) {
			Object startValue = param.get("start");
			Parameter start = parameterContextSerializer.createParameterFromJSONGet(startValue);
			
			Object endValue = param.get("end");
			Parameter end = parameterContextSerializer.createParameterFromJSONGet(endValue);
			
			boolean isStartInclusive = param.getBoolean("isStartInclusive");
			boolean isEndInclusive = param.getBoolean("isEndInclusive");
			boolean isProhibitRange = param.getBoolean("isProhibitRange");
			
			boolean isOptional = param.getBoolean("isOptional");
			return new RangeJobInputParameterImpl(name, type, isOptional, description,
					start, end, isStartInclusive, isEndInclusive, isProhibitRange);
		} else {
			boolean isOptional = param.getBoolean("isOptional");
			return new JobInputParameterImpl(name, type, isOptional, description);
		}
	}
	
	public JSONObject serializeJobInputParameter(JobInputParameter jip) {
		JSONObject param = new JSONObject();
		
		param.put("name", jip.getName());
		param.put("type", jip.getType().toString());
		param.put("description", jip.getDescription());
		
		if (jip instanceof DefaultValueJobInputParameter) {
			DefaultValueJobInputParameter dvp = (DefaultValueJobInputParameter)jip;
			param.put("isDefaultValueImmutable", dvp.isDefaultValueImmutable());
			addParameterValue(param, "defaultValue", dvp.getDefaultValue());
		} else if (jip instanceof EnumJobInputParameter) {
			param.put("isOptional", jip.isOptional());
			
			EnumJobInputParameter ejip = (EnumJobInputParameter)jip;
			param.put("isProhibitValues", ejip.isProhibitValues());
			
			JSONArray valuesJSON = new JSONArray();
			for (Parameter prm : ejip.getValues())
				addParameterValueToArray(valuesJSON, prm);
			param.put("values", valuesJSON);
		} else if (jip instanceof RangeJobInputParameter) {
			param.put("isOptional", jip.isOptional());
			
			RangeJobInputParameter rjip = (RangeJobInputParameter)jip;
			param.put("isProhibitRange", rjip.isProhibitRange());
			
			param.put("isStartInclusive", rjip.isStartInclusive());
			addParameterValue(param, "start", rjip.getStart());
			
			param.put("isEndInclusive", rjip.isEndInclusive());
			addParameterValue(param, "end", rjip.getEnd());
		} else
			param.put("isOptional", jip.isOptional());
		
		return param;
	}

	public void addParameterValue(JSONObject obj, String name, Parameter value) {
		if (value instanceof StringParameter) {
			obj.put(name, ((StringParameter)value).getValue());
		} else if (value instanceof LongParameter) {
			obj.put(name, ((LongParameter)value).getValue());
		} else if (value instanceof DoubleParameter) {
			obj.put(name, ((DoubleParameter)value).getValue());
		} else if (value instanceof BoolParameter) {
			obj.put(name, ((BoolParameter)value).getValue());
		} else if (value instanceof DictionaryParameter) {
			DictionaryParameter dPrm = (DictionaryParameter)value;
			JSONObject dictJSON = new JSONObject();
			for (Entry<String, Parameter> ent : dPrm.getValue().entrySet())
				addParameterValue(dictJSON, ent.getKey(), ent.getValue());
			obj.put(name, dictJSON);
		} else if (value instanceof ListParameter) {
			ListParameter lPrm = (ListParameter)value;
			JSONArray listJSON = new JSONArray();
			for (Parameter prm : lPrm.getValue())
				addParameterValueToArray(listJSON, prm);
			obj.put(name, listJSON);
		}
	}

	public void addParameterValueToArray(JSONArray arr, Parameter value) {
		if (value instanceof StringParameter) {
			arr.put(((StringParameter)value).getValue());
		} else if (value instanceof LongParameter) {
			arr.put(((LongParameter)value).getValue());
		} else if (value instanceof DoubleParameter) {
			arr.put(((DoubleParameter)value).getValue());
		} else if (value instanceof BoolParameter) {
			arr.put(((BoolParameter)value).getValue());
		} else if (value instanceof DictionaryParameter) {
			DictionaryParameter dPrm = (DictionaryParameter)value;
			JSONObject dictJSON = new JSONObject();
			for (Entry<String, Parameter> ent : dPrm.getValue().entrySet())
				addParameterValue(dictJSON, ent.getKey(), ent.getValue());
			arr.put(dictJSON);
		} else if (value instanceof ListParameter) {
			ListParameter lPrm = (ListParameter)value;
			JSONArray listJSON = new JSONArray();
			for (Parameter prm : lPrm.getValue())
				addParameterValueToArray(listJSON, prm);
			arr.put(listJSON);
		}
	}
}
