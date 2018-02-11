package org.werk.config;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.werk.exceptions.WerkConfigException;
import org.werk.meta.JobType;
import org.werk.meta.StepType;
import org.werk.processing.steps.StepExec;
import org.werk.processing.steps.Transitioner;

import lombok.Getter;

public class WerkConfigImpl<J> implements WerkConfig<J> {
	@Getter
	protected Map<String, JobTypeRegistry> jobTypes;
	@Getter
	protected Map<String, StepType<J>> stepTypes;
	
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
	public Collection<JobType> getAllJobTypes() {
		return jobTypes.values().stream()
				.map(a -> a.jobTypeVersions.values())
				.flatMap(x -> x.stream())
				.collect(Collectors.toList());
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

	/*@Override
	public JobType getJobTypeForOldVersion(Long version, String jobTypeName) {
		JobTypeRegistry registry = jobTypes.get(jobTypeName);
		
		if (registry != null)
			if (registry.getMaxVersion() != version)
				return registry.getJobTypeVersions().get(version);
		
		return null;
	}*/

	@Override
	public JobType getJobTypeLatestVersion(String jobTypeName) {
		JobTypeRegistry registry = jobTypes.get(jobTypeName);
		
		if (registry != null)
			return registry.getJobTypeVersions().get(registry.getMaxVersion());
		
		return null;
	}
	
	//-----------------------------------------------
	
	@Override
	public Map<String, StepType<J>> getAllStepTypes() {
		return Collections.unmodifiableMap(stepTypes);
	}
	
	@Override
	public StepType<J> getStepType(String stepTypeName) {
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
	public void addStepType(StepType<J> stepType) throws WerkConfigException {
		if (stepTypes.containsKey(stepType.getStepTypeName()))
			throw new WerkConfigException(
					String.format("Duplicate StepType [%s]", 
							stepType.getStepTypeName())
				);
		stepTypes.put(stepType.getStepTypeName(), stepType);
	}

	//-----------------------------------------------
	
	@Override
	public StepExec<J> getStepExec(String stepType) throws Exception {
		StepType<J> stepTypeObj = getStepType(stepType);
		StepExec<J> stepExec = stepTypeObj.getStepExecFactory().createStepExec();
		
		return stepExec;
	}
	
	@Override
	public Transitioner<J> getStepTransitioner(String stepType) throws Exception {
		StepType<J> stepTypeObj = getStepType(stepType);
		Transitioner<J> stepTransitioner = stepTypeObj.getStepTransitionerFactory().createStepTransitioner();
		
		return stepTransitioner;
	}
}
