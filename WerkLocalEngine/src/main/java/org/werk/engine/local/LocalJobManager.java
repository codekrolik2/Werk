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

import org.pillar.lru.LRUCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.werk.data.StepPOJO;
import org.werk.engine.JobStepFactory;
import org.werk.engine.WerkEngine;
import org.werk.engine.processing.WerkStep;
import org.werk.exceptions.WerkException;
import org.werk.meta.JobInitInfo;
import org.werk.meta.JobReviveInfo;
import org.werk.meta.OldVersionJobInitInfo;
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
	final Logger logger = LoggerFactory.getLogger(LocalJobManager.class);
	
	@AllArgsConstructor
	class JobCluster {
		@Getter
		protected Set<J> jobs;
	}
	
	@Setter
	protected JobStepFactory<J> jobStepFactory;
	@Setter
	protected WerkEngine<J> werkEngine;
	
	protected ReentrantLock lock;
	protected long maxJobCacheSize;
	
	protected Map<J, Job<J>> currentJobs;
	protected Map<J, ReadOnlyJob<J>> finishedJobs;
	protected LRUCache<JobCluster, JobCluster> evictionLRUCache;
	protected AtomicLong cacheSize;
	
	//[ ParentJob : Set [ ChildJob ] ]
	protected Map<J, Set<J>> childJobs;
	protected Map<J, JobCluster> jobClusters;
	
	//[ JoinedJob Id : Set [ Awaiting Job Id ] ]
	protected Map<J, Set<J>> joinedJobs;
	
	public LocalJobManager(long maxJobCacheSize) {
		this(null, null, maxJobCacheSize);
	}
	
	public LocalJobManager(JobStepFactory<J> jobStepFactory, WerkEngine<J> werkEngine,
			long maxJobCacheSize) {
		this.jobStepFactory = jobStepFactory;
		this.werkEngine = werkEngine;
		
		this.maxJobCacheSize = maxJobCacheSize;
		
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
	
	public List<ReadOnlyJob<J>> getAllChildJobs(J jobId) {
		lock.lock();
		try {
			return getJobs(childJobs.get(jobId));
		} finally {
			lock.unlock();
		}
	}

	public List<ReadOnlyJob<J>> getChildJobsOfTypes(J jobId, Set<String> jobTypes) {
		return getAllChildJobs(jobId).stream()
				.filter(a -> jobTypes.contains(a.getJobTypeName()))
				.collect(Collectors.toList());
	}
	
	//---------------------------------------------------
	//JOB EVENTS: JOIN
	
	public void join(J jobId, Collection<J> join) throws Exception {
		lock.lock();
		try {
			Job<J> job = currentJobs.get(jobId);
			if (job == null)
				throw new WerkException(
					String.format("Sanity check failure: Joined job not found in currentJobs: id [%d]", jobId)
				);
			
			Set<J> previousJoin = joinedJobs.get(jobId);
			if (previousJoin != null) 
				throw new WerkException(
					String.format("Sanity check failure: Join record already exists for a job: id [%d] join [%s]", 
							jobId, previousJoin)
				);
			
			Set<J> newJoinSet = join.stream().collect(Collectors.toSet());
			joinedJobs.put(jobId, newJoinSet);
			
			//In case all joined jobs are already done
			checkJoinedJob(jobId);
		} finally {
			lock.unlock();
		}
	}
	
	protected void checkJoinedJob(J joinedJobId) throws WerkException {
		Job<J> job = currentJobs.get(joinedJobId);
		if (job == null)
			throw new WerkException(
				String.format("Sanity check failure: Joined job not found in currentJobs: id [%d]", joinedJobId)
			);
		
		if (job.getStatus() != JobStatus.JOINING)
			throw new WerkException(
					String.format("Sanity check failure: Joined job has status different from JOINING: id [%d] [%s]", 
							joinedJobId, job.getStatus())
				);
		
		if (!job.getJoinStatusRecord().isPresent())
			throw new WerkException(
					String.format("Sanity check failure: Joined job's JoinStatusRecord is not present: id [%d]", 
							joinedJobId)
				);
		
		JoinStatusRecord<J> joinStatusRecord = job.getJoinStatusRecord().get();
		Map<J, JobStatus> jobStatuses = new HashMap<>();
		
		for (J jobId : joinStatusRecord.getJoinedJobs()) {
			if (currentJobs.containsKey(jobId))
				return;
			ReadOnlyJob<J> finishedJob = finishedJobs.get(jobId);
			jobStatuses.put(jobId, finishedJob != null ? finishedJob.getStatus() : JobStatus.INACTIVE); 
		}
		
		LocalJoinResult<J> joinResult = new LocalJoinResult<J>(jobStatuses);
		job.putStringParameter(joinStatusRecord.getJoinParameterName(), job.joinResultToStr(joinResult));
		
		job.setStatus(joinStatusRecord.getStatusBeforeJoin());
		
		werkEngine.addJob(job);
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
			Set<J> joined = joinedJobs.get(jobId);
			for (J joinedJobId : joined)
				checkJoinedJob(joinedJobId);
			joinedJobs.remove(jobId);
		} finally {
			lock.unlock();
		}
	}
	
	//---------------------------------------------------
	//JOB REVIVAL
	
	public void reviveJob(JobReviveInfo<J> init) throws Exception {
		lock.lock();
		try {
			J jobId = init.getJobId();
			
			ReadOnlyJob<J> jobToRevive = finishedJobs.remove(jobId);
			if (jobToRevive == null)
				throw new WerkException(
					String.format("Job not found in finishedJobs: id [%d]", jobId)
				);
			
			LocalWerkJob<J> revivedJob = (LocalWerkJob<J>)jobStepFactory.createJob(jobToRevive);
			revivedJob.setStepCount(((LocalWerkJob<J>)jobToRevive).getStepCount());

			//Update job Parameters
			for (Entry<String, Parameter> jobPrmEntry : init.getJobParametersUpdate().entrySet()) {
				String key = jobPrmEntry.getKey();
				Parameter prm = jobPrmEntry.getValue();
				
				revivedJob.putJobParameter(key, prm);
			}
			
			for (String jobParametersToRemove : init.getJobParametersToRemove())
				revivedJob.removeJobParameter(jobParametersToRemove);
			
			//Copy processing history and set current step
			List<StepPOJO> processingHistory = jobToRevive.getProcessingHistory();
			Step<J> currentStep;
			if (!init.getNewStepTypeName().isPresent()) {
				//Restart current step
				List<StepPOJO> newProcessingHistory = new ArrayList<>();
				
				for (int i = 0; i < processingHistory.size()-1; i++) {
					StepPOJO readOnlyStep = processingHistory.get(i);
					newProcessingHistory.add(readOnlyStep);
				}
				
				processingHistory = newProcessingHistory;
				
				StepPOJO lastStep = processingHistory.get(processingHistory.size()-1);
				currentStep = jobStepFactory.createNewStep(revivedJob, lastStep.getStepNumber(), 
						lastStep.getStepTypeName());
			} else {
				currentStep = jobStepFactory.createNewStep(revivedJob, revivedJob.getNextStepNumber(), 
						init.getNewStepTypeName().get());
			}
			
			revivedJob.setProcessingHistory(processingHistory);
			revivedJob.setCurrentStep((WerkStep<J>)currentStep);
			
			currentJobs.put(jobId, revivedJob);
			
			JobCluster cluster = jobClusters.get(jobId);
			evictionLRUCache.remove(cluster);
			cacheSize.addAndGet(-1L*cluster.jobs.size());
			
			werkEngine.addJob(revivedJob);
		} finally {
			lock.unlock();
		}
	}
	
	//---------------------------------------------------
	//JOB CREATION
	
	public J createJob(JobInitInfo init, Optional<J> parentJob) throws Exception {
		LocalWerkJob<J> job = (LocalWerkJob<J>)jobStepFactory.createNewJob(init.getJobTypeName(), init.getInitParameters(),
				init.getJobName(), parentJob);
		
		lock.lock();
		try {
			addNewJob(job);
		} finally {
			lock.unlock();
		}
		
		return job.getJobId();
	}
	
	public J createOldVersionJob(OldVersionJobInitInfo init, Optional<J> parentJob) throws Exception {
		LocalWerkJob<J> job = (LocalWerkJob<J>)jobStepFactory.createOldVersionJob(init.getJobTypeName(), init.getOldVersion(), 
				init.getInitParameters(), init.getJobName(), parentJob);
		
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
			WerkStep<J> firstStep = (WerkStep<J>)jobStepFactory.createFirstStep(job, job.getNextStepNumber());
			job.setCurrentStep(firstStep);
			
			currentJobs.put(job.getJobId(), job);
			
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
			
			if (jobCluster == null) {
				Set<J> jobs = new HashSet<J>();
				jobCluster = new JobCluster(jobs);
			}
			jobCluster.getJobs().add(job.getJobId());
			
			jobClusters.put(job.getJobId(), jobCluster);
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
}
