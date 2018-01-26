package org.werk.config;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.werk.meta.JobType;
import org.werk.meta.StepType;

import lombok.Getter;

public class WerkConfigImpl implements WerkConfig {
	@Getter
	protected Map<String, JobTypeRegistry> jobTypes;
	@Getter
	protected Map<String, StepType> stepTypes;
	
	class JobTypeRegistry {
		@Getter
		Long maxVersion = null;
		@Getter
		Map<Long, JobType> jobTypeVersions;
		
		public JobTypeRegistry() {
			this.jobTypeVersions = new HashMap<>();
		}
		
		public void addJobType(JobType jobType) throws WerkConfigException {
			long version = jobType.getVersion();
			if (jobTypeVersions.containsKey(version))
				throw new WerkConfigException(
						String.format("Duplicate version for JobType [%s] Version [%d]", 
								jobType.getJobTypeName(), version)
					);
			
			jobTypeVersions.put(version, jobType);
			if ((maxVersion == null) || (maxVersion < version))
				maxVersion = version;
		}
	}
	
	public WerkConfigImpl() {
		jobTypes = new HashMap<>();
		stepTypes = new HashMap<>();
	}

	@Override
	public Collection<String> getAllJobTypeNames() {
		return Collections.unmodifiableCollection(jobTypes.keySet());
	}

	@Override
	public Collection<Long> getJobTypeVersions(String jobTypeName) {
		JobTypeRegistry registry = jobTypes.get(jobTypeName);
		return registry == null ? null : Collections.unmodifiableCollection(registry.jobTypeVersions.keySet());
	}

	@Override
	public JobType getJobTypeForAnyVersion(Long version, String jobTypeName) {
		JobTypeRegistry registry = jobTypes.get(jobTypeName);
		
		if (registry != null)
			return registry.getJobTypeVersions().get(version);
		
		return null;
	}

	@Override
	public JobType getJobTypeForOldVersion(Long version, String jobTypeName) {
		JobTypeRegistry registry = jobTypes.get(jobTypeName);
		
		if (registry != null)
			if (registry.getMaxVersion() != version)
				return registry.getJobTypeVersions().get(version);
		
		return null;
	}

	@Override
	public JobType getJobTypeLatestVersion(String jobTypeName) {
		JobTypeRegistry registry = jobTypes.get(jobTypeName);
		
		if (registry != null)
			return registry.getJobTypeVersions().get(registry.getMaxVersion());
		
		return null;
	}
	
	//-----------------------------------------------
	
	@Override
	public Map<String, StepType> getAllStepTypes() {
		return Collections.unmodifiableMap(stepTypes);
	}
	
	@Override
	public StepType getStepType(String stepTypeName) {
		return stepTypes.get(stepTypeName);
	}
	
	//-----------------------------------------------
	
	@Override
	public void addJobType(JobType jobType) throws WerkConfigException {
		JobTypeRegistry registry = jobTypes.get(jobType.getJobTypeName());
		if (registry == null) {
			registry = new JobTypeRegistry();
			jobTypes.put(jobType.getJobTypeName(), registry);
		}
		
		registry.addJobType(jobType);
	}
	
	@Override
	public void addStepType(StepType stepType) throws WerkConfigException {
		if (stepTypes.containsKey(stepType.getStepTypeName()))
			throw new WerkConfigException(
					String.format("Duplicate StepType [%s]", 
							stepType.getStepTypeName())
				);
		stepTypes.put(stepType.getStepTypeName(), stepType);
	}
}
