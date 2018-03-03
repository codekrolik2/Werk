package org.werk.rest.pojo;

import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.werk.meta.JobTypeSignature;
import org.werk.meta.OverflowAction;
import org.werk.meta.StepExecFactory;
import org.werk.meta.StepTransitionerFactory;
import org.werk.meta.StepTypeImpl;

import lombok.Getter;

public class RESTStepType<J> extends StepTypeImpl<J> {
	@Getter
	JSONObject jsonObj;
	@Getter
	Set<JobTypeSignature> jobTypes;

	public RESTStepType(JSONObject jsonObj, String stepTypeName, Set<JobTypeSignature> jobTypes, 
			Set<String> allowedTransitions, Set<String> allowedRollbackTransitions,
			StepExecFactory<J> stepExecFactory, StepTransitionerFactory<J> stepTransitionerFactory,
			String processingDescription, String rollbackDescription, String execConfig, String transitionerConfig,
			long logLimit, OverflowAction logOverflowAction, boolean shortTransaction) {
		super(stepTypeName, 
			null,
			allowedTransitions, allowedRollbackTransitions, stepExecFactory, stepTransitionerFactory,
			processingDescription, rollbackDescription, execConfig, transitionerConfig, logLimit, logOverflowAction,
			shortTransaction);
		this.jsonObj = jsonObj;
		this.jobTypes = jobTypes;
	}
	
	public String getStepExecName() {
		return stepExecFactory.getStepExecClassName();
	}
	
	public String getTransitionerName() {
		return stepTransitionerFactory.getTransitionerClassName();
	}
	
	public String getStepExecShortName() {
		String longName = stepExecFactory.getStepExecClassName();
		int start = longName.lastIndexOf('.');
		return longName.substring(start < 0 ? 0 : start + 1);
	}
	
	public String getTransitionerShortName() {
		String longName = stepTransitionerFactory.getTransitionerClassName();
		int start = longName.lastIndexOf('.');
		return longName.substring(start < 0 ? 0 : start + 1);
	}
	
	public String getJobTypesStr() {
		return String.join(", ", jobTypes.stream().
				map(a -> JobTypeSignature.getJobTypeFullName(a)).
				collect(Collectors.toList()));
	}
	
	public String getTransitions() {
		return String.join(", ", allowedTransitions);
	}
	
	public String getRollbackTransitions() {
		return String.join(", ", allowedRollbackTransitions);
	}
}
