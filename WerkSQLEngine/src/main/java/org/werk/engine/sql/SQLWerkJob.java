package org.werk.engine.sql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.log4j.Logger;
import org.pillar.db.interfaces.TransactionContext;
import org.pillar.db.interfaces.TransactionFactory;
import org.pillar.time.interfaces.Timestamp;
import org.werk.config.WerkConfig;
import org.werk.data.StepPOJO;
import org.werk.engine.json.JoinResultSerializer;
import org.werk.engine.processing.WerkJob;
import org.werk.engine.sql.DAO.DBJobPOJO;
import org.werk.engine.sql.DAO.DBReadOnlyJob;
import org.werk.engine.sql.DAO.JobDAO;
import org.werk.engine.sql.DAO.StepDAO;
import org.werk.meta.JobInitInfo;
import org.werk.meta.JobReviveInfo;
import org.werk.meta.JobType;
import org.werk.meta.OldVersionJobInitInfo;
import org.werk.processing.jobs.JobStatus;
import org.werk.processing.jobs.JoinStatusRecord;
import org.werk.processing.parameters.Parameter;
import org.werk.processing.readonly.ReadOnlyJob;

import lombok.Getter;
import lombok.Setter;

public class SQLWerkJob extends WerkJob<Long> {
	final Logger logger = Logger.getLogger(SQLWerkJob.class);
	
	@Getter
	protected Long jobId;
	
	@Getter
	protected TransactionFactory transactionFactory;
	
	@Getter @Setter
	protected TransactionContext stepTransactionContext = null;
	
	protected StepDAO stepDAO;
	protected JobDAO jobDAO;
	protected WerkConfig<Long> werkConfig; 
	
	@Getter
	protected Set<Long> forkedJobs;
	
	public SQLWerkJob(Long jobId, JobType jobType, long version, Optional<String> jobName, JobStatus status,
			Map<String, Parameter> jobInitialParameters, Map<String, Parameter> jobParameters,
			Timestamp nextExecutionTime, Optional<JoinStatusRecord<Long>> joinStatusRecord,
			Optional<Long> parentJobId, int stepCount, JoinResultSerializer<Long> joinResultSerializer, 
			TransactionFactory transactionFactory, StepDAO stepDAO, JobDAO jobDAO, WerkConfig<Long> werkConfig) {
		super(jobType, version, jobName, status, jobInitialParameters, jobParameters, nextExecutionTime, 
				joinStatusRecord, parentJobId, stepCount, joinResultSerializer);
		this.jobId = jobId;
		this.transactionFactory = transactionFactory;
		this.stepDAO = stepDAO;
		this.jobDAO = jobDAO;
		this.werkConfig = werkConfig;
		forkedJobs = new HashSet<>();
	}

	public TransactionContext getOrCreateTC() throws Exception {
		if (stepTransactionContext != null)
			return stepTransactionContext;
		else
			return transactionFactory.startTransaction();
	}
	
	public void closeTCIfNeeded(TransactionContext tc) {
		if (tc != null)
			if (stepTransactionContext != null)
				try { tc.close(); } catch (Exception e) { logger.error("Transaction close error", e); }
	}
	
	@Override
	public Collection<StepPOJO> getProcessingHistory() throws Exception {
		TransactionContext tc = null;
		try {
			tc = getOrCreateTC();
			
			return stepDAO.getProcessingHistory(tc, jobId, Optional.empty(), Optional.empty(), Optional.empty());
		} catch(Exception e) {
			throw e;
		} finally {
			closeTCIfNeeded(tc);
		}
	}

	@Override
	public Collection<StepPOJO> getFilteredHistory(String stepTypeName) throws Exception {
		TransactionContext tc = null;
		try {
			tc = getOrCreateTC();
			
			return stepDAO.getProcessingHistory(tc, jobId, Optional.of(stepTypeName), Optional.empty(), Optional.empty());
		} catch(Exception e) {
			throw e;
		} finally {
			closeTCIfNeeded(tc);
		}
	}

	@Override
	public StepPOJO getStep(long stepNumber) throws Exception {
		TransactionContext tc = null;
		try {
			tc = getOrCreateTC();
			
			Collection<StepPOJO> steps = stepDAO.getProcessingHistory(tc, jobId, Optional.empty(), Optional.of(stepNumber), Optional.empty());
			if ((steps == null) || (steps.isEmpty()))
				return null;
			
			for (StepPOJO step : steps)
				return step;
			
			return null;
		} catch(Exception e) {
			throw e;
		} finally {
			closeTCIfNeeded(tc);
		}
	}
	
	//----------------------------------------------
	@Override
	public Long fork(JobInitInfo jobInitInfo) throws Exception {
		TransactionContext tc = null;
		try {
			tc = getOrCreateTC();
			
			long jobId = jobDAO.createJob(tc, jobInitInfo, JobStatus.UNDEFINED.getCode(), Optional.of(getJobId()), 0);
			
			String firstStepType = werkConfig.getJobTypeLatestVersion(jobInitInfo.getJobTypeName()).getFirstStepTypeName();
			
			long firstStepId = stepDAO.createProcessingStep(tc, jobId, firstStepType, 0);
			
			jobDAO.updateFirstStep(tc, jobId, firstStepId);
			
			forkedJobs.add(jobId);
			
			return jobId;
		} catch(Exception e) {
			throw e;
		} finally {
			closeTCIfNeeded(tc);
		}
	}
	
	@Override
	public Long forkOldVersion(OldVersionJobInitInfo jobInitInfo) throws Exception {
		TransactionContext tc = null;
		try {
			tc = getOrCreateTC();
			
			long jobId = jobDAO.createOldVersionJob(tc, jobInitInfo, JobStatus.UNDEFINED.getCode(), Optional.of(getJobId()), 0);
			
			String firstStepType = werkConfig.getJobTypeForAnyVersion(version, jobInitInfo.getJobTypeName()).getFirstStepTypeName();
			
			long firstStepId = stepDAO.createProcessingStep(tc, jobId, firstStepType, 0);
			
			jobDAO.updateFirstStep(tc, jobId, firstStepId);
			
			forkedJobs.add(jobId);
			
			return jobId;
		} catch(Exception e) {
			throw e;
		} finally {
			closeTCIfNeeded(tc);
		}
	}
	
	@Override
	public void revive(JobReviveInfo<Long> jobReviveInfo) throws Exception {
		TransactionContext tc = null;
		try {
			tc = getOrCreateTC();
			
			jobDAO.reviveJob(tc, jobReviveInfo);
		} catch(Exception e) {
			throw e;
		} finally {
			closeTCIfNeeded(tc);
		}
	}
	
	//----------------------------------------------
	
	@Override
	public ReadOnlyJob<Long> loadJob(Long jobId) throws Exception {
		List<Long> jobIds = new ArrayList<>();
		jobIds.add(jobId);
		
		List<ReadOnlyJob<Long>> jobs = loadJobs(jobIds);
		if ((jobs == null) || (jobs.isEmpty()))
			return null;
		else
			return jobs.get(0);
	}

	@Override
	public List<ReadOnlyJob<Long>> loadJobs(Collection<Long> jobIds) throws Exception {
		return loadJobs(Optional.of(jobIds), Optional.empty(), Optional.empty());
	}

	@Override
	public List<ReadOnlyJob<Long>> loadAllChildJobs() throws Exception {
		List<Long> parentJobIds = new ArrayList<Long>();
		parentJobIds.add(getJobId());
		return loadJobs(Optional.empty(), Optional.of(parentJobIds), Optional.empty());
	}

	@Override
	public List<ReadOnlyJob<Long>> loadChildJobsOfTypes(Set<String> jobTypes) throws Exception {
		return loadJobs(Optional.empty(), Optional.empty(), Optional.of(jobTypes));
	}

	protected List<ReadOnlyJob<Long>> loadJobs(Optional<Collection<Long>> jobIds, Optional<Collection<Long>> parentJobId, 
			Optional<Set<String>> jobTypes) throws Exception {
		TransactionContext tc = null;
		try {
			tc = getOrCreateTC();
			
			List<Long> jobIdList = new ArrayList<>();
			jobIdList.add(jobId);
			Collection<DBJobPOJO> jobs = jobDAO.loadJobs(tc, Optional.empty(), Optional.empty(), 
					jobIds, parentJobId, jobTypes);
			if ((jobs == null) || (jobs.isEmpty()))
				return null;
			
			List<ReadOnlyJob<Long>> readOnlyJobs = new ArrayList<>();
			for (DBJobPOJO job : jobs)
				readOnlyJobs.add(new DBReadOnlyJob(job, transactionFactory, stepTransactionContext, stepDAO));
			
			return readOnlyJobs;
		} catch(Exception e) {
			throw e;
		} finally {
			closeTCIfNeeded(tc);
		}
	}
}
