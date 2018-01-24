package org.werk.config;

import java.util.HashMap;
import java.util.Map;

import org.werk.config.WerkConfig;
import org.werk.meta.JobType;
import org.werk.meta.StepType;

public class WerkConfigImpl implements WerkConfig {
	protected Map<String, JobType> jobTypes;
	protected Map<String, Map<String, StepType>> stepTypes;
	
	public WerkConfigImpl() {
		jobTypes = new HashMap<>();
		stepTypes = new HashMap<>();
	}

	public WerkConfigImpl(Map<String, JobType> jobTypes, 
			Map<String, Map<String, StepType>> stepTypes) {
		this.jobTypes = jobTypes;
		this.stepTypes = stepTypes;
	}

	@Override
	public Map<String, JobType> getJobTypes() {
		return jobTypes;
	}

	@Override
	public Map<String, Map<String, StepType>> getStepTypes() {
		return stepTypes;
	}
	
	public void validate() throws WerkConfigException {
		for (Map.Entry<String, JobType> ent : getJobTypes().entrySet()) {
			String key = ent.getKey();
			JobType type = ent.getValue();

			Map<String, StepType> stepsMap = getStepTypes().get(key);
			if ((stepsMap == null) || stepsMap.isEmpty())
				throw new WerkConfigException(
					String.format("Steps were not found for JobType [%s]", type.getJobTypeName())
				);
			
			if (!stepsMap.containsKey(type.getFirstStepTypeName()))
				throw new WerkConfigException(
						String.format("First step wasn't found for JobType [%s], FirstStep [%s]", 
							type.getJobTypeName(), type.getFirstStepTypeName())
					);
		}
	}
}
