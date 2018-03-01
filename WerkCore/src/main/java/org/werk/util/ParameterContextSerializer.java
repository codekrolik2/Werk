package org.werk.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONObject;
import org.werk.processing.parameters.DictionaryParameter;
import org.werk.processing.parameters.ListParameter;
import org.werk.processing.parameters.Parameter;
import org.werk.processing.parameters.ParameterType;
import org.werk.processing.parameters.impl.BoolParameterImpl;
import org.werk.processing.parameters.impl.DictionaryParameterImpl;
import org.werk.processing.parameters.impl.DoubleParameterImpl;
import org.werk.processing.parameters.impl.ListParameterImpl;
import org.werk.processing.parameters.impl.LongParameterImpl;
import org.werk.processing.parameters.impl.StringParameterImpl;

/*
 * -----
 * Long.parseLong(Double.toString(10))
 * will fail because Double.toString will produce 10.0
 */
public class ParameterContextSerializer {
	public JSONObject serializeParameters(Map<String, Parameter> parameters) {
		JSONObject parameterContext = new JSONObject();
		
		if (parameters != null)
		for (Entry<String, Parameter> ent : parameters.entrySet()) {
			Object val = serializeParameter(ent.getValue());
			if (val != null)
				parameterContext.put(ent.getKey(), val);
		}
		
		return parameterContext;
	}
	
	public Object serializeParameter(Parameter prm) {
		if (prm.getType() == ParameterType.DICTIONARY) {
			return serializeParameters(((DictionaryParameter)prm).getValue());
		} else if (prm.getType() == ParameterType.LIST) {
			return serializeListParameter((ListParameter)prm);
		} else {
			Object parameterValue = ParameterUtils.getParameterValue(prm);
			if (parameterValue == null)
				return null;
			
			if (prm.getType() == ParameterType.LONG)
				return "L " + parameterValue.toString();
			else if (prm.getType() == ParameterType.STRING)
				return "S " + parameterValue.toString();
			else if (prm.getType() == ParameterType.DOUBLE)
				return "D " + parameterValue.toString();
			else if (prm.getType() == ParameterType.BOOL)
				return "B " + parameterValue.toString();
			else
				throw new IllegalArgumentException("Unknown parameter type " + prm.getType());
		} 
	}
	
	protected JSONArray serializeListParameter(ListParameter listParameter) {
		JSONArray listJSON = new JSONArray();
		
		for (Parameter prm : listParameter.getValue())
			listJSON.put(serializeParameter(prm));
		
		return listJSON;
	}
	
	//-----------------------------
	
	public Parameter createParameterFromJSONGet(Object value) {
		if ((value == null) || (value == JSONObject.NULL))
			return null;
		
		if (value instanceof String) {
			String str = (String)value;
			if (str.startsWith("L ")) {
				return new LongParameterImpl(Long.parseLong(str.substring(2)));
			} else if (str.startsWith("S ")) {
				return new StringParameterImpl(str.substring(2));
			} else if (str.startsWith("D ")) {
				return new DoubleParameterImpl(Double.parseDouble(str.substring(2)));
			} else if (str.startsWith("B ")) {
				return new BoolParameterImpl(Boolean.parseBoolean(str.substring(2)));
			} else
				return new StringParameterImpl(str);
	    } else if (value instanceof Integer) {
	    	return new LongParameterImpl((long)(Integer)value);
	    } else if (value instanceof Long) {
	    	return new LongParameterImpl((Long)value);
	    } else if (value instanceof Float) {
	    	return new DoubleParameterImpl((double)((Float)value));
	    } else if (value instanceof Double) {
	    	return new DoubleParameterImpl((Double)value);
	    } else if (value instanceof Boolean) {
	    	return new BoolParameterImpl((Boolean)value);
	    } else if (value instanceof JSONArray) {
	    	List<Parameter> lst = new ArrayList<>();
	    	
	    	for (int i = 0; i < ((JSONArray)value).length(); i++) {
	    		Object lstValue = ((JSONArray)value).get(i);
	    		
			    Parameter p = createParameterFromJSONGet(lstValue);
			    if (p != null)
			    	lst.add(p);
	    	}
	    	
	    	return new ListParameterImpl(lst);
	    } else if (value instanceof JSONObject) {
	    	Map<String, Parameter> map = deserializeParameters((JSONObject)value);
	    	return new DictionaryParameterImpl(map);
	    } else
	    	throw new IllegalArgumentException(
    			String.format("Unknown parameter type: [%s]", value.getClass())
    		);
	}
	
	public Map<String, Parameter> deserializeParameters(JSONObject parametersJSON) {
		Map<String, Parameter> parameterMap = new HashMap<>();
		
		Iterator<?> keys = parametersJSON.keys();
		while (keys.hasNext()) {
		    String key = (String)keys.next();
		    Object value = parametersJSON.get(key);

		    Parameter p = createParameterFromJSONGet(value);
		    if (p != null)
		    	parameterMap.put(key, p);
		}
		
		return parameterMap;
	}
}
