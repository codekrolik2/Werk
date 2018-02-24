package org.werk.engine.sql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.json.JSONObject;
import org.pillar.db.interfaces.TransactionContext;
import org.pillar.db.interfaces.TransactionFactory;
import org.pillar.time.interfaces.Timestamp;
import org.pulse.interfaces.PulseReg;
import org.pulse.interfaces.ServerPulseRecord;
import org.werk.config.WerkConfig;
import org.werk.engine.sql.DAO.DBJobPOJO;
import org.werk.engine.sql.DAO.DBReadOnlyJob;
import org.werk.engine.sql.DAO.JobDAO;
import org.werk.engine.sql.DAO.StepDAO;
import org.werk.engine.sql.main.SQLWerkRunner;
import org.werk.meta.JobInitInfo;
import org.werk.meta.JobRestartInfo;
import org.werk.meta.JobType;
import org.werk.meta.StepType;
import org.werk.meta.VersionJobInitInfo;
import org.werk.processing.jobs.JobStatus;
import org.werk.processing.readonly.ReadOnlyJob;
import org.werk.service.JobCollection;
import org.werk.service.PageInfo;
import org.werk.service.WerkService;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SQLWerkService implements WerkService<Long> {
	protected WerkConfig<Long> werkConfig;
	protected JobDAO jobDAO;
	protected StepDAO stepDAO;
	protected TransactionFactory transactionFactory;
	protected SQLWerkRunner sqlWerkRunner;
	
	@Override
	public Long createJob(JobInitInfo init) throws Exception {
		TransactionContext tc = null;
		try {
			tc = transactionFactory.startTransaction();
			long jobId = jobDAO.createJob(tc, init, JobStatus.PROCESSING.getCode(), Optional.empty(), 0);
			
			String firstStepType = werkConfig.getJobTypeLatestVersion(init.getJobTypeName()).getFirstStepTypeName();
			long firstStepId = stepDAO.createProcessingStep(tc, jobId, firstStepType, 0);
			jobDAO.updateFirstStep(tc, jobId, firstStepId);
			
			tc.commit();
			
			return jobId;
		} finally {
			tc.close();
		}
	}

	@Override
	public Long createJobOfVersion(VersionJobInitInfo init) throws Exception {
		TransactionContext tc = null;
		try {
			tc = transactionFactory.startTransaction();
			long jobId = jobDAO.createJobOfVersion(tc, init, JobStatus.PROCESSING.getCode(), Optional.empty(), 0);

			String firstStepType = werkConfig.getJobTypeLatestVersion(init.getJobTypeName()).getFirstStepTypeName();
			long firstStepId = stepDAO.createProcessingStep(tc, jobId, firstStepType, 0);
			jobDAO.updateFirstStep(tc, jobId, firstStepId);
			
			tc.commit();
			
			return jobId;
		} finally {
			tc.close();
		}
	}

	@Override
	public void restartJob(JobRestartInfo<Long> jobRestartInfo) throws Exception {
		TransactionContext tc = null;
		try {
			tc = transactionFactory.startTransaction();
			jobDAO.restartJob(tc, jobRestartInfo);
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
	public JobCollection<Long> getJobs(Optional<Timestamp> from, Optional<Timestamp> to, 
			Optional<Timestamp> fromExec, Optional<Timestamp> toExec, Optional<Map<String, Long>> jobTypesAndVersions,
			Optional<Collection<Long>> parentJobIds, Optional<Collection<Long>> jobIds, Optional<Set<String>> currentStepTypes, 
			Optional<PageInfo> pageInfo) throws Exception {
		TransactionContext tc = null;
		try {
			tc = transactionFactory.startTransaction();
			JobCollection<Long> jobs = jobDAO.loadJobs(tc, from, to, fromExec, toExec, jobIds, parentJobIds, jobTypesAndVersions, 
					currentStepTypes, pageInfo);
			return jobs;
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

	@Override
	public Collection<StepType<Long>> getAllStepTypes() {
		return werkConfig.getAllStepTypes().values();
	}

	@Override
	public void jobsAdded() {
		SQLWerkEngine engine = sqlWerkRunner.getCurrentEngine().get();
		if (engine != null)
			engine.getSqlJobLoaderRunnable().startJobLoad();
	}

	@Override
	public JSONObject getServerInfo() {
		JSONObject info = new JSONObject();
		info.put("server", "SQL");

		PulseReg<Long> pulse = sqlWerkRunner.getPulse();
		Optional<ServerPulseRecord<Long>> activeServerOpt = pulse.getActiveServerPulseRecord();
		if (activeServerOpt.isPresent()) {
			ServerPulseRecord<Long> srv = activeServerOpt.get();

			JSONObject srvRecord = new JSONObject();
			srvRecord.put("serverId", srv.getServerId());
			srvRecord.put("creationTime", srv.getCreationTime());
			srvRecord.put("lastHBTime", srv.getLastHBTime());
			srvRecord.put("hbPeriodMs", srv.getHBPeriodMs());
			srvRecord.put("info", srv.getInfo());
			
			info.put("srvRecord", srvRecord);
		}
		
		return info;
	}

	@Override
	public Collection<JobType> getJobTypesForStep(String stepTypeName) {
		return werkConfig.getAllJobTypes();
	}
}
