package org.werk.rest.pojo;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;
import org.werk.meta.JobTypeImpl;
import org.werk.meta.OverflowAction;
import org.werk.meta.inputparameters.JobInputParameter;

import lombok.Getter;

public class RESTJobType extends JobTypeImpl {
	@Getter
	JSONObject json;
	
	public RESTJobType(JSONObject json, String jobTypeName, Set<String> stepTypes, Map<String, List<JobInputParameter>> initParameters,
			String firstStepTypeName, String description, String jobConfig, boolean forceAcyclic, long version,
			long historyLimit, OverflowAction historyOverflowAction) {
		super(jobTypeName, stepTypes, initParameters, firstStepTypeName, description, jobConfig, forceAcyclic, version,
				historyLimit, historyOverflowAction);
		this.json = json;
	}

	public String getFullName() {
		return String.format("%s [v%d]", jobTypeName, version);
	}
}
