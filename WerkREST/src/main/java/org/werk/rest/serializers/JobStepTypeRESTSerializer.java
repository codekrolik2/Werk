package org.werk.rest.serializers;

import java.util.List;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONObject;
import org.werk.meta.JobType;
import org.werk.meta.StepType;
import org.werk.meta.inputparameters.DefaultValueJobInputParameter;
import org.werk.meta.inputparameters.EnumJobInputParameter;
import org.werk.meta.inputparameters.JobInputParameter;
import org.werk.meta.inputparameters.RangeJobInputParameter;
import org.werk.processing.parameters.BoolParameter;
import org.werk.processing.parameters.DictionaryParameter;
import org.werk.processing.parameters.DoubleParameter;
import org.werk.processing.parameters.ListParameter;
import org.werk.processing.parameters.LongParameter;
import org.werk.processing.parameters.Parameter;
import org.werk.processing.parameters.StringParameter;

public class JobStepTypeRESTSerializer {
	public static JSONObject serializeJobType(JobType jobType) {
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
		jobTypeJSON.put("historyOverflowAction", jobType.getHistoryOverflowAction());
		
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

	public static JSONObject serializeStepType(StepType<?> stepType) {
		JSONObject jobTypeJSON = new JSONObject();
		
		jobTypeJSON.put("stepTypeName", stepType.getStepTypeName());
		
		jobTypeJSON.put("allowedTransitions", stepType.getAllowedTransitions());
		jobTypeJSON.put("allowedRollbackTransitions", stepType.getAllowedRollbackTransitions());
		
		jobTypeJSON.put("stepExec", stepType.getStepExecFactory().getStepExecClass());
		jobTypeJSON.put("transitioner", stepType.getStepTransitionerFactory().getTransitionerClass());

		//-------------------------
		
		jobTypeJSON.put("processingDescription", stepType.getProcessingDescription());
		jobTypeJSON.put("rollbackDescription", stepType.getRollbackDescription());

		jobTypeJSON.put("execConfig", stepType.getExecConfig());
		jobTypeJSON.put("transitionerConfig", stepType.getTransitionerConfig());
		
		jobTypeJSON.put("logLimit", stepType.getLogLimit());
		jobTypeJSON.put("logOverflowAction", stepType.getLogOverflowAction());

		jobTypeJSON.put("shortTransaction", stepType.isShortTransaction());
		
		return jobTypeJSON;
	}
	
	public static JSONObject serializeJobInputParameter(JobInputParameter jip) {
		JSONObject param = new JSONObject();
		
		param.put("name", jip.getName());
		param.put("type", jip.getType());
		param.put("isOptional", jip.isOptional());
		param.put("description", jip.getDescription());
		
		if (jip instanceof DefaultValueJobInputParameter) {
			DefaultValueJobInputParameter dvp = (DefaultValueJobInputParameter)jip;
			param.put("isDefaultValueImmutable", dvp.isDefaultValueImmutable());
			addParameterValue(param, "defaultValue", dvp.getDefaultValue());
		} else if (jip instanceof EnumJobInputParameter) {
			EnumJobInputParameter ejip = (EnumJobInputParameter)jip;
			param.put("isProhibitValues", ejip.isProhibitValues());
			
			JSONArray valuesJSON = new JSONArray();
			for (Parameter prm : ejip.getValues())
				addParameterValueToArray(valuesJSON, prm);
			param.put("values", valuesJSON);
		} else if (jip instanceof RangeJobInputParameter) {
			RangeJobInputParameter rjip = (RangeJobInputParameter)jip;
			param.put("isProhibitRange", rjip.isProhibitRange());
			
			param.put("isStartInclusive", rjip.isStartInclusive());
			addParameterValue(param, "start", rjip.getStart());
			
			param.put("isEndInclusive", rjip.isEndInclusive());
			addParameterValue(param, "end", rjip.getEnd());
		}
		
		return param;
	}
	
	public static void addParameterValue(JSONObject obj, String name, Parameter value) {
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

	public static void addParameterValueToArray(JSONArray arr, Parameter value) {
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
