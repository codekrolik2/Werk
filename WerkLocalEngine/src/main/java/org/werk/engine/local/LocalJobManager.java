package org.werk.engine.local;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.pillar.lru.LRUCache;
import org.werk.data.StepPOJO;
import org.werk.engine.WerkEngine;
import org.werk.engine.processing.WerkStep;
import org.werk.exceptions.WerkException;
import org.werk.meta.JobInitInfo;
import org.werk.meta.JobRestartInfo;
import org.werk.meta.NewStepRestartInfo;
import org.werk.meta.VersionJobInitInfo;
import org.werk.processing.jobs.Job;
import org.werk.processing.jobs.JobStatus;
import org.werk.processing.jobs.JoinStatusRecord;
import org.werk.processing.parameters.Parameter;
import org.werk.processing.readonly.ReadOnlyJob;
import org.werk.processing.steps.Step;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

public class LocalJobManager<J> {
	final Logger logger = Logger.getLogger(LocalJobManager.class);
	
	@AllArgsConstructor
	class JobCluster {
		@Getter
		protected Set<J> jobs;
	}
	
	@Setter
	protected LocalJobStepFactory<J> jobStepFactory;
	@Setter
	protected WerkEngine<J> werkEngine;
	
	protected ReentrantLock lock;
	protected int maxJobCacheSize;
	
	protected Map<J, Job<J>> currentJobs;
	protected Map<J, ReadOnlyJob<J>> finishedJobs;
	protected LRUCache<JobCluster, JobCluster> evictionLRUCache;
	protected AtomicLong cacheSize;
	
	//[ ParentJob : Set [ ChildJob ] ]
	protected Map<J, Set<J>> childJobs;
	protected Map<J, JobCluster> jobClusters;
	
	//[ JoinedJob Id : Set [ Awaiting Job Id ] ]
	protected Map<J, Set<J>> joinedJobs;
	
	public LocalJobManager(int maxJobCacheSize) {
		this(null, null, maxJobCacheSize);
	}
	
	public LocalJobManager(LocalJobStepFactory<J> jobStepFactory, WerkEngine<J> werkEngine,
			int maxJobCacheSize) {
		this.jobStepFactory = jobStepFactory;
		this.werkEngine = werkEngine;
		
		this.maxJobCacheSize = maxJobCacheSize;
		cacheSize = new AtomicLong(0);
		
		lock = new ReentrantLock();
		
		currentJobs = new HashMap<>();
		childJobs = new HashMap<>();
		jobClusters = new HashMap<>();
		
		finishedJobs = new HashMap<>();
		
		joinedJobs = new HashMap<>();
		
		evictionLRUCache = new LRUCache<JobCluster, JobCluster>(lock) {
			public void init(final int capacity) {
				lruData = new LinkedHashMap<JobCluster, JobCluster>(capacity + 1, 1.0f, true) {
					private static final long serialVersionUID = 7293575125752194475L;

					protected boolean removeEldestEntry(Map.Entry<JobCluster, JobCluster> entry) {
						if (cacheSize.get() > maxJobCacheSize) {
							lock.lock();
							try {
								if (cacheSize.get() > maxJobCacheSize) {
									JobCluster jobCluster = entry.getValue();
									
									cacheSize.addAndGet(-1*jobCluster.getJobs().size());
									
									for (J jobId : jobCluster.getJobs()) {
										finishedJobs.remove(jobId);
										childJobs.remove(jobId);
										jobClusters.remove(jobId);
									}
									
									return true;
								}
							} finally {
								lock.unlock();
							}
						}
						return false;
					};
				};
			}
		};
		
		evictionLRUCache.init(maxJobCacheSize);
	}
	
	//---------------------------------------------------
	//JOB RETRIEVAL
	
	public ReadOnlyJob<J> getJob(J jobId) {
		lock.lock();
		try {
			ReadOnlyJob<J> readOnlyJob = currentJobs.get(jobId);
			if (readOnlyJob != null)
				return readOnlyJob;
			
			readOnlyJob = finishedJobs.get(jobId);
			return readOnlyJob;
		} finally {
			lock.unlock();
		}
	}
	
	public List<ReadOnlyJob<J>> getAllJobs() {
		lock.lock();
		try {
			return Stream.concat(currentJobs.values().stream(), finishedJobs.values().stream()).
				collect(Collectors.toList());
		} finally {
			lock.unlock();
		}
	}
	
	public List<ReadOnlyJob<J>> getJobs(Collection<J> jobIds) {
		lock.lock();
		try {
			List<ReadOnlyJob<J>> jobs = new ArrayList<ReadOnlyJob<J>>();
			
			if (jobIds != null) {
				for (J jobId : jobIds) {
					ReadOnlyJob<J> job = getJob(jobId);
					if (job != null)
						jobs.add(job);
				}
			}
			
			return jobs;
		} finally {
			lock.unlock();
		}
	}
	
	public Collection<ReadOnlyJob<J>> getAllChildJobs(Collection<J> jobIds) {
		lock.lock();
		try {
			Set<ReadOnlyJob<J>> roJobs = new HashSet<>();
			for (J jobId : jobIds)
				roJobs.addAll(getJobs(childJobs.get(jobId)));
			return roJobs;
		} finally {
			lock.unlock();
		}
	}

	public List<ReadOnlyJob<J>> getChildJobsOfTypes(Collection<J> jobIds, Map<String, Long> jobTypesAndVersions) {
		return getAllChildJobs(jobIds).stream()
				.filter(a -> 
					jobTypesAndVersions.containsKey(a.getJobTypeName())
					&& (
						jobTypesAndVersions.get(a.getJobTypeName()).equals(a.getVersion())
						||
						jobTypesAndVersions.get(a.getJobTypeName()).compareTo(0L) <= 0
					)
				)
				.collect(Collectors.toList());
	}
	
	//---------------------------------------------------
	//JOB EVENTS: JOIN
	
	public void join(J awaitingJobId, Collection<J> join) throws Exception {
		lock.lock();
		try {
			Job<J> job = currentJobs.get(awaitingJobId);
			if (job == null)
				throw new WerkException(
					String.format("Sanity check failure: Joined job not found in currentJobs: id [%d]", awaitingJobId)
				);
			
			for (J joinedJob : join) {
				Set<J> set = joinedJobs.get(joinedJob);
				if (set == null) {
					set = new HashSet<>();
					joinedJobs.put(joinedJob, set);
				}
				set.add(awaitingJobId);
			}
			
			//In case all joined jobs are already done
			checkJoinedJob(awaitingJobId);
		} finally {
			lock.unlock();
		}
	}
	
	protected void checkJoinedJob(J awaitingJobId) throws WerkException {
		Job<J> job = currentJobs.get(awaitingJobId);
		if (job == null)
			throw new WerkException(
				String.format("Sanity check failure: Awaiting job not found in currentJobs: id [%d]", awaitingJobId)
			);
		
		if (job.getStatus() != JobStatus.JOINING)
			throw new WerkException(
					String.format("Sanity check failure: Awaiting job has status different from JOINING: id [%d] [%s]", 
							awaitingJobId, job.getStatus())
				);
		
		if (!job.getJoinStatusRecord().isPresent())
			throw new WerkException(
					String.format("Sanity check failure: Awaiting job's JoinStatusRecord is not present: id [%d]", 
							awaitingJobId)
				);
		
		JoinStatusRecord<J> joinStatusRecord = job.getJoinStatusRecord().get();
		
		int finishedJobCount = 0;
		for (J jobId : joinStatusRecord.getJoinedJobIds())
			if (!currentJobs.containsKey(jobId))
				finishedJobCount++;
		
		boolean waitDone = joinStatusRecord.getWaitForNJobs() <= finishedJobCount;
		
		if (waitDone) {
			for (J jobId : joinStatusRecord.getJoinedJobIds()) {
				Set<J> awaitingJobs = joinedJobs.get(jobId);
				awaitingJobs.remove(awaitingJobId);
				if (awaitingJobs.isEmpty())
					joinedJobs.remove(jobId);
			}
			
			job.getCurrentStep().putStringParameter(joinStatusRecord.getJoinParameterName(), job.joinResultToStr(joinStatusRecord));
			((LocalWerkJob<J>)job).setStatus(job.getCurrentStep().isRollback() ? JobStatus.ROLLING_BACK : JobStatus.PROCESSING);
			
			werkEngine.addJob(job);
		}
	}
	
	//---------------------------------------------------
	//JOB EVENTS: END OF PROCESSING
	
	public void jobFinished(J jobId) throws Exception {
		moveJobToProcessed(jobId);
	}
	
	public void jobFailed(J jobId) throws Exception {
		moveJobToProcessed(jobId);
	}
	
	protected void moveJobToProcessed(J jobId) throws Exception {
		lock.lock();
		try {
			Job<J> job = currentJobs.remove(jobId);
			if (job == null)
				throw new WerkException(
					String.format("Job not found in currentJobs: id [%d]", jobId)
				);
			
			finishedJobs.put(jobId, job);
			
			//If cluster of jobs is done, move the cluster to eviction cache 
			JobCluster cluster = jobClusters.get(jobId);
			boolean allDone = true;
			for (J clusterJobId : cluster.getJobs())
				if (currentJobs.containsKey(clusterJobId))
					allDone = false;
			
			if (allDone) {
				evictionLRUCache.put(cluster, cluster);
				cacheSize.addAndGet(cluster.jobs.size());
			}
			
			//Check joined jobs
			Set<J> awaitingThisJob = joinedJobs.get(jobId);
			if (awaitingThisJob != null)
				for (J awaitingJobId : awaitingThisJob)
					checkJoinedJob(awaitingJobId);
			joinedJobs.remove(jobId);
		} finally {
			lock.unlock();
		}
	}
	
	//---------------------------------------------------
	//JOB REVIVAL
	
	public void restartJob(JobRestartInfo<J> init) throws Exception {
		lock.lock();
		try {
			J jobId = init.getJobId();
			
			ReadOnlyJob<J> jobToRestart = finishedJobs.remove(jobId);
			if (jobToRestart == null)
				throw new WerkException(
					String.format("Job not found in finishedJobs: id [%d]", jobId)
				);
			
			LocalWerkJob<J> restartedJob = (LocalWerkJob<J>)jobStepFactory.createJob(jobToRestart);
			restartedJob.setStepCount(((LocalWerkJob<J>)jobToRestart).getStepCount());
			
			//Update job init Parameters
			Map<String, Parameter> newInitParameters = new HashMap<>();
			newInitParameters.putAll(restartedJob.getJobInitialParameters());
			
			for (Entry<String, Parameter> jobPrmEntry : init.getJobInitParametersUpdate().entrySet()) {
				String key = jobPrmEntry.getKey();
				Parameter prm = jobPrmEntry.getValue();
				
				newInitParameters.put(key, prm);
			}
			
			for (String jobInitParametersToRemove : init.getJobInitParametersToRemove())
				newInitParameters.remove(jobInitParametersToRemove);
			
			restartedJob.setJobInitialParameters(newInitParameters);
			
			//Update job Parameters
			for (Entry<String, Parameter> jobPrmEntry : init.getJobParametersUpdate().entrySet()) {
				String key = jobPrmEntry.getKey();
				Parameter prm = jobPrmEntry.getValue();
				
				restartedJob.putJobParameter(key, prm);
			}
			
			for (String jobParametersToRemove : init.getJobParametersToRemove())
				restartedJob.removeJobParameter(jobParametersToRemove);
			
			//Copy processing history and set current step
			Collection<StepPOJO> processingHistory = jobToRestart.getProcessingHistory();
			Step<J> currentStep;
			if (!init.getNewStepInfo().isPresent()) {
				//Restart current step
				List<StepPOJO> newProcessingHistory = new ArrayList<>();
				
				StepPOJO lastStep = null;
				for (StepPOJO readOnlyStep : processingHistory) {
					if (lastStep != null)
						newProcessingHistory.add(lastStep);
					lastStep = readOnlyStep;
				}
				
				if (lastStep.isRollback())
					restartedJob.setStatus(JobStatus.ROLLING_BACK);
				else
					restartedJob.setStatus(JobStatus.PROCESSING);
				
				processingHistory = newProcessingHistory;
				
				currentStep = jobStepFactory.createNewStep(restartedJob, lastStep.getStepNumber(), 
						Optional.ofNullable(lastStep.getRollbackStepNumbers()), lastStep.getStepTypeName());
			} else {
				//Create a new step
				NewStepRestartInfo stepInfo = init.getNewStepInfo().get();
				
				if (stepInfo.isNewStepRollback())
					restartedJob.setStatus(JobStatus.ROLLING_BACK);
				else
					restartedJob.setStatus(JobStatus.PROCESSING);
				
				currentStep = jobStepFactory.createNewStep(restartedJob, restartedJob.getNextStepNumber(), 
						stepInfo.getStepsToRollback(), stepInfo.getNewStepTypeName());
			}
			
			processingHistory.add(currentStep);
			restartedJob.setProcessingHistory(processingHistory);
			restartedJob.setCurrentStep((WerkStep<J>)currentStep);
			
			//Update currentStep Parameters
			for (Entry<String, Parameter> stepPrmEntry : init.getStepParametersUpdate().entrySet()) {
				String key = stepPrmEntry.getKey();
				Parameter prm = stepPrmEntry.getValue();
				
				currentStep.putStepParameter(key, prm);
			}
			
			for (String stepParameterToRemove : init.getStepParametersToRemove())
				currentStep.removeStepParameter(stepParameterToRemove);
			
			currentJobs.put(jobId, restartedJob);
			
			JobCluster cluster = jobClusters.get(jobId);
			evictionLRUCache.remove(cluster);
			cacheSize.addAndGet(-1L*cluster.jobs.size());
			
			if (init.getJoinStatusRecord().isPresent()) {
				JoinStatusRecord<J> rec = init.getJoinStatusRecord().get();
				
				((LocalWerkJob<J>)restartedJob).setJoinStatusRecord(init.getJoinStatusRecord());
				((LocalWerkJob<J>)restartedJob).setStatus(JobStatus.JOINING);

				join(restartedJob.getJobId(), rec.getJoinedJobIds());
			} else
				werkEngine.addJob(restartedJob);
		} finally {
			lock.unlock();
		}
	}
	
	//---------------------------------------------------
	//JOB CREATION
	
	public J createJob(JobInitInfo init, Optional<J> parentJob) throws Exception {
		LocalWerkJob<J> job = (LocalWerkJob<J>)jobStepFactory.createNewJob(init.getJobTypeName(), 
				init.getInitSignatureName(), init.getInitParameters(),
				init.getJobName(), init.getNextExecutionTime(), parentJob);
		
		lock.lock();
		try {
			addNewJob(job);
		} finally {
			lock.unlock();
		}
		
		return job.getJobId();
	}
	
	public J createJobOfVersion(VersionJobInitInfo init, Optional<J> parentJob) throws Exception {
		LocalWerkJob<J> job = (LocalWerkJob<J>)jobStepFactory.createJobOfVersion(init.getJobTypeName(), init.getJobVersion(),
				init.getInitSignatureName(),
				init.getInitParameters(), init.getJobName(), init.getNextExecutionTime(), parentJob);
		
		lock.lock();
		try {
			addNewJob(job);
		} finally {
			lock.unlock();
		}
	
		return job.getJobId();
	}
	
	protected void addNewJob(LocalWerkJob<J> job) throws Exception {
		JobCluster jobCluster = null;
		try {
			//Init first step
			WerkStep<J> firstStep = (WerkStep<J>)jobStepFactory.createFirstStep(job, job.getNextStepNumber());
			job.setCurrentStep(firstStep);
			
			currentJobs.put(job.getJobId(), job);
			
			//Child jobs
			if (job.getParentJobId().isPresent()) {
				J parentJob = job.getParentJobId().get();
				
				Set<J> childJobSet = childJobs.get(parentJob);
				if (childJobSet == null) {
					childJobSet = new HashSet<>();
					childJobs.put(parentJob, childJobSet);
				}
				childJobSet.add(job.getJobId());
				
				jobCluster = jobClusters.get(parentJob);
			}
			
			//Create or add job cluster
			if (jobCluster == null) {
				Set<J> jobs = new HashSet<J>();
				jobCluster = new JobCluster(jobs);
			}
			jobCluster.getJobs().add(job.getJobId());
			jobClusters.put(job.getJobId(), jobCluster);

			//Set job status to PROCESSING
			job.setStatus(JobStatus.PROCESSING);
			
			werkEngine.addJob(job);
		} catch (Exception e) {
			currentJobs.remove(job.getJobId());
			
			if (job.getParentJobId().isPresent()) {
				J parentJob = job.getParentJobId().get();
				childJobs.remove(parentJob);
			}
			
			if (jobCluster != null)
				jobCluster.getJobs().remove(job.getJobId());
			
			jobClusters.remove(job.getJobId());
			throw e;
		} 
	}
	
	//---------------------------------------------------
	
	public int getJobCount() {
		return currentJobs.size();
	}
}
