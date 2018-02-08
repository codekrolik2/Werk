package org.werk.engine.sql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.pillar.db.interfaces.TransactionContext;
import org.pillar.db.interfaces.TransactionFactory;
import org.pillar.exec.workdistribution.FairWorkDistributionCalc;
import org.pillar.exec.workdistribution.Worker;
import org.pulse.interfaces.Pulse;
import org.pulse.interfaces.ServerPulseRecord;
import org.pulse.jdbc.JDBCServerPulseDAO;
import org.werk.engine.json.JoinResultSerializer;
import org.werk.engine.sql.DAO.DBJobPOJO;
import org.werk.engine.sql.DAO.DBStepPOJO;
import org.werk.engine.sql.DAO.JobDAO;
import org.werk.engine.sql.DAO.JobLoadDAO;
import org.werk.engine.sql.DAO.StepDAO;
import org.werk.processing.jobs.JobStatus;
import org.werk.processing.jobs.JoinStatusRecord;
import org.werk.processing.parameters.impl.StringParameterImpl;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class SQLJobLoader {
	final Logger logger = Logger.getLogger(SQLJobLoader.class);

	@AllArgsConstructor
	class MyWorker implements Worker {
		@Getter
		Optional<Long> workUnitLimit;
		@Getter
		long ownedWorkUnits;
	};
	
	protected JobLoadDAO jobLoadDAO;
	protected JobDAO jobDAO;
	protected StepDAO stepDAO;
	protected JDBCServerPulseDAO pulseDAO;
	
	protected JoinResultSerializer<Long> joinResultSerializer;
	protected FairWorkDistributionCalc workCalc;
	protected TransactionFactory factory;
	protected Pulse<Long> pulse;
	
	protected void unlockJoinedJob(TransactionContext tc, long jobId) throws Exception {
		try {
			//2.1 Get job / JoinStatusRecord
			DBJobPOJO job = jobDAO.loadJob(tc, jobId);
			
			if (job.getStatus() != JobStatus.JOINING)
				throw new Exception(
					String.format("Job Status is not JOINING [%s]",
							job.getJobId(), job.getStatus())
				);
			
			JoinStatusRecord<Long> rec = job.getJoinStatusRecord().get();
			if (rec == null)
				throw new Exception("Job doesn't have JoinStatusRecord");

			//2.2 Set STEP parameter: join result
			DBStepPOJO step = (DBStepPOJO)stepDAO.getStep(tc, jobId, job.getCurrentStepId());
			step.getStepParameters().put(rec.getJoinParameterName(),
					new StringParameterImpl(joinResultSerializer.serializeJoinResult(rec).toString()));
			stepDAO.updateStep(tc, step.getStepId(), step.getExecutionCount(), step.getStepParameters(), 
					step.getProcessingLog());
			
			//2.3 Delete join_record_jobs records
			jobLoadDAO.deleteJoinRecords(tc, jobId);
			
			//2.4 Update job status, remove JoinStatusRecord
			jobDAO.updateJob(tc, jobId, step.getStepId(), rec.getStatusBeforeJoin(), job.getNextExecutionTime(),
					job.getJobParameters(), job.getStepCount(), Optional.empty());
			
			tc.commit();
		} catch (Exception e) {
			logger.info(String.format("Failed to join-unlock job id [%d]", jobId), e);
			return;
		}
	}
	
	protected boolean outOfTime(long startTime, long maxExecutionTime) {
		return (System.currentTimeMillis() <= startTime + maxExecutionTime);
	}
	
	protected void unlockJoinedJobs(TransactionContext tc, long startTime, long maxExecutionTime) throws Exception {
		while (true) {
			if (outOfTime(startTime, maxExecutionTime)) break;
			
			//1. Find a list of unlockable joined jobs
			Set<Long> unlockableJobs = jobLoadDAO.getUnlockableJoinedJobs(tc);
			if (unlockableJobs.isEmpty()) break;
			
			for (long jobId : unlockableJobs) {
				unlockJoinedJob(tc, jobId);
				if (outOfTime(startTime, maxExecutionTime)) break;
			}
		}
	}
	
	protected void loadJob(TransactionContext tc, long jobId) throws Exception {
		try {
			//Load current job and step
			DBJobPOJO job = jobDAO.loadJob(tc, jobId);
			
			if (job.getIdLocker().isPresent())
				throw new Exception(String.format("Job already owned by [%d]", job.getIdLocker().get()));
				
			if ((job.getStatus() != JobStatus.PROCESSING) && (job.getStatus() != JobStatus.ROLLING_BACK))
				throw new Exception(String.format("Can't load job with status [%s]", job.getStatus()));
			
			DBStepPOJO currentStep = (DBStepPOJO)stepDAO.getStep(tc, jobId, job.getCurrentStepId());
			
			//Remove all child jobs in UNDEFINED state
			jobLoadDAO.deleteUnconfirmedForkedChildJobs(tc, jobId);
			
			//TODO: create SQLWerkJob and WerkStep
			
			//TODO: Add job to WerkEngine
			
			tc.commit();
		} catch (Exception e) {
			logger.info(String.format("Failed to load job id [%d]", jobId), e);
			return;
		}
	}
	
	public void loadJobs(long heartbeatPeriod, long maxExecutionTime) throws Exception {
		long startTime = System.currentTimeMillis();
		
		TransactionContext tc = null;
		try {
			Optional<ServerPulseRecord<Long>> pulseRecordOpt = pulse.getActiveServerPulseRecord();
			if (pulseRecordOpt.isPresent()) {
				ServerPulseRecord<Long> selfFromPulse = pulseRecordOpt.get();
				
				//1. Load server list
				tc = factory.startTransaction();
				List<ServerPulseRecord<Long>> servers = pulseDAO.getAllServers(tc);

				//1.1 Fill workers, self
				List<Worker> workers = new ArrayList<>();
				Worker self = null;
				
				for (ServerPulseRecord<Long> server : servers) {
					try {
						JSONObject srvInfo = new JSONObject(server.getInfo());
						
						long ownedWorkUnits = srvInfo.getLong("jobCount");
						Optional<Long> workUnitLimit = srvInfo.has("jobLimit") ? 
								Optional.of(srvInfo.getLong("jobLimit")) : Optional.empty();
	
						MyWorker worker = new MyWorker(workUnitLimit, ownedWorkUnits);
						if (server.getServerId() == selfFromPulse.getServerId())
							self = worker;
					} catch(JSONException je) {
						//Do nothing: If serverInfo is not compliant with the format, the server won't be 
						//considered as a part of job distribution simulation 
					}
				}
				
				//1.2 If local server pulse record not found, lose heartbeat
				if (self == null)
					throw new Exception(
							String.format("Local server record not found in servers table: [%d]", selfFromPulse.getServerId())
						);
				
				//2. Determine how many jobs can be loaded
				//JobId, NextExecTime
				Map<Long, Long> loadableJobIds = jobLoadDAO.getLoadableJobs(tc);
				
				List<Long> oldJobIds = new ArrayList<>();
				List<Long> newJobIds = new ArrayList<>();
				for (Map.Entry<Long, Long> ent : loadableJobIds.entrySet()) {
					long jobId = ent.getKey();
					long nextExecutionTime = ent.getValue();
					
					if (nextExecutionTime < startTime - 4*heartbeatPeriod)
						oldJobIds.add(jobId);
					else
						newJobIds.add(jobId);
				}
				Collections.shuffle(newJobIds);
				
				long jobLoadCount = workCalc.calculate(newJobIds.size(), workers, self);
				
				//3. Load jobs
				//3.1. Load all old jobs and add to WerkEngine
				for (long jobId : oldJobIds)
					loadJob(tc, jobId);
				//3.2. Load N new jobs and add to WerkEngine
				for (int i = 0; i < jobLoadCount; i++)
					loadJob(tc, newJobIds.get(i));
				
				//4. Unlock up to jobLimit of joined jobs that can be unlocked
				//	(Step 1 should work even if pulse has failed)
				// Limit number of jobs
				// Loop until there are jobs or until average unlock time > wait time until next execution
				unlockJoinedJobs(tc, startTime, maxExecutionTime);
			}
		} catch (Exception e) {
			logger.error("Jobs load failure, losing Heartbeat", e);
			pulse.loseHeartbeat(e);
		} finally {
			if (tc != null)
				tc.close();
		}
	}
}
