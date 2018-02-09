package org.werk.engine.sql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.pillar.db.interfaces.TransactionContext;
import org.pillar.db.interfaces.TransactionFactory;
import org.pillar.time.interfaces.Timestamp;
import org.werk.config.WerkConfig;
import org.werk.data.JobPOJO;
import org.werk.engine.sql.DAO.DBJobPOJO;
import org.werk.engine.sql.DAO.DBReadOnlyJob;
import org.werk.engine.sql.DAO.JobDAO;
import org.werk.engine.sql.DAO.StepDAO;
import org.werk.meta.JobInitInfo;
import org.werk.meta.JobReviveInfo;
import org.werk.meta.JobType;
import org.werk.meta.OldVersionJobInitInfo;
import org.werk.meta.StepType;
import org.werk.processing.jobs.JobStatus;
import org.werk.processing.readonly.ReadOnlyJob;
import org.werk.service.WerkService;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SQLWerkService implements WerkService<Long> {
	protected WerkConfig<Long> werkConfig;
	protected JobDAO jobDAO;
	protected StepDAO stepDAO;
	protected TransactionFactory transactionFactory;
	
	@Override
	public Long createJob(JobInitInfo init) throws Exception {
		TransactionContext tc = null;
		try {
			tc = transactionFactory.startTransaction();
			long jobId = jobDAO.createJob(tc, init, JobStatus.PROCESSING.getCode(), Optional.empty(), 0);
			tc.commit();
			
			return jobId;
		} finally {
			tc.close();
		}
	}

	@Override
	public Long createOldVersionJob(OldVersionJobInitInfo init) throws Exception {
		TransactionContext tc = null;
		try {
			tc = transactionFactory.startTransaction();
			long jobId = jobDAO.createOldVersionJob(tc, init, JobStatus.PROCESSING.getCode(), Optional.empty(), 0);
			tc.commit();
			
			return jobId;
		} finally {
			tc.close();
		}
	}

	@Override
	public void reviveJob(JobReviveInfo<Long> jobReviveInfo) throws Exception {
		TransactionContext tc = null;
		try {
			tc = transactionFactory.startTransaction();
			jobDAO.reviveJob(tc, jobReviveInfo);
			tc.commit();
		} finally {
			tc.close();
		}
	}

	@Override
	public ReadOnlyJob<Long> getJobAndHistory(Long jobId) throws Exception {
		TransactionContext tc = null;
		try {
			tc = transactionFactory.startTransaction();
			DBJobPOJO jobPojo = jobDAO.loadJob(tc, jobId);
			DBReadOnlyJob roJob = new DBReadOnlyJob(jobPojo, transactionFactory, null, stepDAO);
			return roJob;
		} finally {
			tc.close();
		}
	}

	@Override
	public Collection<JobPOJO<Long>> getJobs(Optional<Timestamp> from, Optional<Timestamp> to, Optional<Set<String>> jobTypes,
			Optional<Collection<Long>> jobIds) throws Exception {
		TransactionContext tc = null;
		try {
			tc = transactionFactory.startTransaction();
			@SuppressWarnings({ "unchecked", "rawtypes" })
			Collection<JobPOJO<Long>> jobs = (Collection)jobDAO.loadJobs(tc, from, to, jobIds, Optional.empty(), jobTypes);
			return jobs;
		} finally {
			tc.close();
		}
	}

	@Override
	public Collection<JobPOJO<Long>> getChildJobs(Long jobId) throws Exception {
		TransactionContext tc = null;
		try {
			tc = transactionFactory.startTransaction();
			@SuppressWarnings({ "unchecked", "rawtypes" })
			Collection<JobPOJO<Long>> jobs = (Collection)jobDAO.loadJobs(tc, Optional.empty(), Optional.empty(),
					Optional.empty(), Optional.of(jobId), Optional.empty());
			return jobs;
		} finally {
			tc.close();
		}
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Collection<ReadOnlyJob<Long>> getChildJobsAndHistory(Long jobId) throws Exception {
		TransactionContext tc = null;
		try {
			tc = transactionFactory.startTransaction();
			Collection<DBJobPOJO> jobs = jobDAO.loadJobs(tc, Optional.empty(), Optional.empty(),
					Optional.empty(), Optional.of(jobId), Optional.empty());
			
			List<DBReadOnlyJob> roJobs = new ArrayList<DBReadOnlyJob>();
			for (DBJobPOJO jobPojo : jobs) {
				DBReadOnlyJob roJob = new DBReadOnlyJob(jobPojo, transactionFactory, null, stepDAO);
				roJobs.add(roJob);
			}
			
			return (Collection<ReadOnlyJob<Long>>)(Collection)roJobs;
		} finally {
			tc.close();
		}
	}

	//-------------------------------------------------------------
	
	@Override
	public Collection<JobType> getJobTypes() {
		return werkConfig.getAllJobTypes();
	}

	@Override
	public JobType getJobType(String jobTypeName, Optional<Long> version) {
		if (version.isPresent())
			return werkConfig.getJobTypeLatestVersion(jobTypeName);
		else
			return werkConfig.getJobTypeForAnyVersion(version.get(), jobTypeName);
	}

	@Override
	public Collection<StepType<Long>> getStepTypesForJob(String jobTypeName, Optional<Long> version) {
		JobType jobType = getJobType(jobTypeName, version);
		
		List<StepType<Long>> stepTypes = new ArrayList<>();
		for (String stepTypeName : jobType.getStepTypes()) {
			StepType<Long> stepType = getStepType(stepTypeName);
			stepTypes.add(stepType);
		}
		
		return stepTypes;
	}

	@Override
	public StepType<Long> getStepType(String stepTypeName) {
		return werkConfig.getStepType(stepTypeName);
	}
}
