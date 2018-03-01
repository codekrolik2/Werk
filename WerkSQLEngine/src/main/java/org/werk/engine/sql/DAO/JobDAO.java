package org.werk.engine.sql.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.json.JSONObject;
import org.pillar.db.interfaces.TransactionContext;
import org.pillar.db.jdbc.JDBCTransactionContext;
import org.pillar.db.jdbc.JDBCTransactionFactory;
import org.pillar.time.LongTimeProvider;
import org.pillar.time.LongTimestamp;
import org.pillar.time.interfaces.TimeProvider;
import org.pillar.time.interfaces.Timestamp;
import org.werk.config.WerkConfig;
import org.werk.data.JobPOJO;
import org.werk.data.StepPOJO;
import org.werk.engine.json.JobParameterTool;
import org.werk.engine.sql.exception.JobRestartException;
import org.werk.engine.sql.exception.JoinStateException;
import org.werk.exceptions.WerkConfigException;
import org.werk.meta.JobInitInfo;
import org.werk.meta.JobRestartInfo;
import org.werk.meta.JobType;
import org.werk.meta.NewStepRestartInfo;
import org.werk.meta.VersionJobInitInfo;
import org.werk.meta.inputparameters.JobInputParameter;
import org.werk.processing.jobs.JobStatus;
import org.werk.processing.jobs.JoinStatusRecord;
import org.werk.processing.jobs.MapJoinStatusRecord;
import org.werk.processing.parameters.Parameter;
import org.werk.service.JobCollection;
import org.werk.service.PageInfo;
import org.werk.util.ParameterContextSerializer;

public class JobDAO {
	protected TimeProvider timeProvider; 
	protected ParameterContextSerializer parameterContextSerializer;
	
	protected WerkConfig<Long> werkConfig;
	protected StepDAO stepDAO;
	protected JobParameterTool jobParameterTool = new JobParameterTool();
	
	public JobDAO(TimeProvider timeProvider, ParameterContextSerializer parameterContextSerializer, 
			WerkConfig<Long> werkConfig, StepDAO stepDAO) {
		this.timeProvider = timeProvider;
		this.parameterContextSerializer = parameterContextSerializer;
		this.werkConfig = werkConfig;
		this.stepDAO = stepDAO;
	}
	
	public Long updateFirstStep(TransactionContext tc, long jobId, long newStepId) throws Exception {
		Connection connection = ((JDBCTransactionContext)tc).getConnection();
		PreparedStatement pst = null;
		
		try {
			pst = connection.prepareStatement("UPDATE jobs SET current_step_id = ?, step_count = ? WHERE id_job = ?");
			
			pst.setLong(1, newStepId);
			pst.setInt(2, 1);
			pst.setLong(3, jobId);
			
			pst.executeUpdate();
			
			long serverId = JDBCTransactionFactory.getLastId(connection);
			return serverId;
		} finally {
			if (pst != null) pst.close();
		}
	}
	
	public Long createJob(TransactionContext tc, JobInitInfo init, int jobStatus, Optional<Long> parentJob, 
			int stepCount) throws Exception {
		JobType jobType = werkConfig.getJobTypeLatestVersion(init.getJobTypeName());
		if (jobType == null)
			throw new WerkConfigException(
				String.format("JobType not found [%s]", init.getJobTypeName())
			);
		
		if (!init.getInitSignatureName().isPresent()) {
			if (!jobParameterTool.emptyInitParameterSetAllowed(jobType))
				throw new WerkConfigException(
						String.format("Empty init parameter set is not allowed for JobType [%s] version [%d]",
								init.getJobTypeName(), jobType.getVersion())
					);
		} else {
			//Check initial parameters
			List<JobInputParameter> parameterSet = jobParameterTool.getParameterSet(jobType, init.getInitSignatureName().get());
			if (parameterSet == null)
				throw new WerkConfigException(
						String.format("Init signature [%s] is not found for JobType [%s] version [%d]", 
								init.getInitSignatureName().get(), init.getJobTypeName(), jobType.getVersion())
					);
			
			//Check enum and range parameters
			jobParameterTool.checkParameters(parameterSet, init.getInitParameters());
			
			//Fill default parameters
			jobParameterTool.fillParameters(parameterSet, init.getInitParameters());
		}
		
		LongTimestamp nextExecutionTime = init.getNextExecutionTime().isPresent() ? 
				(LongTimestamp)init.getNextExecutionTime().get() : ((LongTimestamp)timeProvider.getCurrentTime());
		
		return createJob(tc, init.getJobTypeName(), jobType.getVersion(), init.getJobName(), 
				parentJob, jobStatus, nextExecutionTime.getTimeMs(), 
				init.getInitParameters(), stepCount);
	}
	
	public Long createJobOfVersion(TransactionContext tc, VersionJobInitInfo init, int jobStatus, Optional<Long> parentJob, 
			int stepCount) throws Exception {
		JobType jobType = werkConfig.getJobTypeForAnyVersion(init.getJobVersion(), init.getJobTypeName());
		if (jobType == null)
			throw new WerkConfigException(
				String.format("JobType not found [%s] version [%d]", init.getJobTypeName(), init.getJobVersion())
			);

		if (!init.getInitSignatureName().isPresent()) {
			if (!jobParameterTool.emptyInitParameterSetAllowed(jobType))
				throw new WerkConfigException(
					String.format("Empty init parameter set is not allowed for JobType [%s] version [%d]",
							init.getJobTypeName(), init.getJobVersion())
				);
		} else {
			//Check initial parameters
			List<JobInputParameter> parameterSet = jobParameterTool.getParameterSet(jobType, init.getInitSignatureName().get());
			if (parameterSet == null)
				throw new WerkConfigException(
						String.format("Init signature [%s] is not found for JobType [%s] version [%d]", 
								init.getInitSignatureName().get(), init.getJobTypeName(), init.getJobVersion())
					);
			
			//Check enum and range parameters
			jobParameterTool.checkParameters(parameterSet, init.getInitParameters());
			
			//Fill default parameters
			jobParameterTool.fillParameters(parameterSet, init.getInitParameters());
		}
		
		LongTimestamp nextExecutionTime = init.getNextExecutionTime().isPresent() ? 
				(LongTimestamp)init.getNextExecutionTime().get() : ((LongTimestamp)timeProvider.getCurrentTime());

		return createJob(tc, init.getJobTypeName(), init.getJobVersion(), init.getJobName(), 
				parentJob, jobStatus, nextExecutionTime.getTimeMs(), 
				init.getInitParameters(), stepCount);
	}
	
	public Long createJob(TransactionContext tc, String jobTypeName, long version, Optional<String> jobName,
			Optional<Long> parentJobId, int status, long nextExecutionTime, Map<String, Parameter> jobInitialParameters, 
			int stepCount) throws Exception {
		String jobInitialParameterState = parameterContextSerializer.serializeParameters(jobInitialParameters).toString();
		
		Connection connection = ((JDBCTransactionContext)tc).getConnection();
		PreparedStatement pst = null;
		
		try {
			String query = "INSERT INTO jobs (job_type, version, job_name, parent_job_id, " +
					"status, next_execution_time, job_parameter_state, job_initial_parameter_state, step_count, creation_time) " +
					"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			
			pst = connection.prepareStatement(query);
			
			pst.setString(1, jobTypeName);
			pst.setLong(2, version);
			
			if (jobName.isPresent())
				pst.setString(3, jobName.get());
			else
				pst.setNull(3, java.sql.Types.VARCHAR);
			
			if (parentJobId.isPresent())
				pst.setLong(4, parentJobId.get());
			else
				pst.setNull(4, java.sql.Types.BIGINT);
			
			pst.setInt(5, status);
			pst.setLong(6, nextExecutionTime);
			pst.setString(7, jobInitialParameterState);
			pst.setString(8, jobInitialParameterState);
			pst.setLong(9, stepCount);
			pst.setLong(10, ((LongTimestamp)(timeProvider.getCurrentTime())).getTimeMs());
			
			pst.executeUpdate();
			
			long serverId = JDBCTransactionFactory.getLastId(connection);
			return serverId;
		} finally {
			if (pst != null) pst.close();
		}
	}

	/*
	current_step_id
	status
	step_count
	next_execution_time
	job_parameter_state

	wait_for_N_jobs
	status_before_join
	join_parameter_name
	*/
	public int updateJob(TransactionContext tc, long jobId, long currentStepId, JobStatus status, Timestamp nextExecutionTime,
		Map<String, Parameter> jobParameters, int stepCount, Optional<JoinStatusRecord<Long>> joinStatusRecord) throws Exception {
		if (joinStatusRecord.isPresent()) {
			DBJobPOJO job = loadJob(tc, jobId);
			if (job == null)
				throw new JoinStateException(
						String.format("Job not found: [%d]", jobId)
					);
			
			if (job.getStatus() == JobStatus.JOINING)
				throw new JoinStateException(
						String.format("Job is already in JOINING state: [%d]", jobId)
					);
		}
		
		Connection connection = ((JDBCTransactionContext)tc).getConnection();
		PreparedStatement pst = null;
		
		try {
			String query;
			if (joinStatusRecord.isPresent()) {
				query = "UPDATE jobs SET current_step_id = ?, status = ?, next_execution_time = ?, job_parameter_state = ?, " + 
						"step_count = ?, wait_for_N_jobs = ?, join_parameter_name = ? " + 
						"WHERE id_job = ?";
			} else {
				query = "UPDATE jobs SET current_step_id = ?, status = ?, next_execution_time = ?, job_parameter_state = ?, " + 
						"step_count = ? " + 
						"WHERE id_job = ?";
			}
			
			pst = connection.prepareStatement(query);
			
			String jobParametersStr = parameterContextSerializer.serializeParameters(jobParameters).toString();
			
			pst.setLong(1, currentStepId);
			pst.setInt(2, status.getCode());
			pst.setLong(3, ((LongTimestamp)nextExecutionTime).getTimeMs());
			pst.setString(4, jobParametersStr);
			pst.setInt(5, stepCount);
			
			if (joinStatusRecord.isPresent()) {
				pst.setLong(6, joinStatusRecord.get().getWaitForNJobs());
				pst.setString(7, joinStatusRecord.get().getJoinParameterName());
				pst.setLong(8, jobId);
			} else
				pst.setLong(6, jobId);
			
			int recordsUpdated = pst.executeUpdate();
			
			if (joinStatusRecord.isPresent())
				for (Long joinedJobId : joinStatusRecord.get().getJoinedJobIds())
					recordsUpdated += createJoinRecordJob(tc, jobId, joinedJobId);
			
			return recordsUpdated;
		} finally {
			if (pst != null) pst.close();
		}
	}
	
	public int createJoinRecordJob(TransactionContext tc, long awaitingJobId, long jobId) throws SQLException {
		Connection connection = ((JDBCTransactionContext)tc).getConnection();
		PreparedStatement pst = null;
		
		try {
			String query = "INSERT INTO join_record_jobs (id_awaiting_job, id_job) " +
					"VALUES (?, ?)";
			
			pst = connection.prepareStatement(query);
			
			pst.setLong(1, awaitingJobId);
			pst.setLong(2, jobId);
			
			return pst.executeUpdate();
		} finally {
			if (pst != null) pst.close();
		}
	}
	
	public void restartJob(TransactionContext tc, JobRestartInfo<Long> init) throws Exception {
		PreparedStatement pst = null;
		
		try {
			//1. Load job from DB
			long jobId = init.getJobId();
			DBJobPOJO job = loadJob(tc, jobId);
			
			if (job == null)
				throw new JobRestartException(
						String.format("Job not found: [%d]", jobId)
					);
			
			if ((job.getStatus() != JobStatus.FINISHED) && (job.getStatus() != JobStatus.ROLLED_BACK) && 
				(job.getStatus() != JobStatus.FAILED))
				throw new JobRestartException(
						String.format("Job can't be restarted: not in final state: [%d] [%s]", jobId, job.getStatus())
					);
			
			//2.1. Set updated job init parameters
			Map<String, Parameter> newInitParameters = new HashMap<>();
			newInitParameters.putAll(job.getJobInitialParameters());
			
			for (Map.Entry<String, Parameter> ent : init.getJobInitParametersUpdate().entrySet())
				newInitParameters.put(ent.getKey(), ent.getValue());
			for (String jobInitParameterToRemove : init.getJobInitParametersToRemove())
				newInitParameters.remove(jobInitParameterToRemove);
			
			job.setJobInitialParameters(newInitParameters);
			
			//2.2. Set updated job parameters
			Map<String, Parameter> jobParameters = job.getJobParameters();
			
			for (Map.Entry<String, Parameter> ent : init.getJobParametersUpdate().entrySet())
				jobParameters.put(ent.getKey(), ent.getValue());
			for (String jobParameterToRemove : init.getJobParametersToRemove())
				jobParameters.remove(jobParameterToRemove);
			
			JobStatus jobStatus = job.getStatus();
			long newStepId = job.getCurrentStepId();
			
			int stepCount = job.getStepCount();
			
			//3. Create new step or update existing step
			if (init.getNewStepInfo().isPresent()) {
				//3.1 Create new step
				NewStepRestartInfo newStepInfo = init.getNewStepInfo().get();

				//Init step parameters
				Map<String, Parameter> stepParameters = init.getStepParametersUpdate();
				for (String stepParameterToRemove : init.getStepParametersToRemove())
					stepParameters.remove(stepParameterToRemove);
				
				//Create step
				if (newStepInfo.isNewStepRollback()) {
					jobStatus = JobStatus.ROLLING_BACK;
					newStepId = stepDAO.createRollbackStep(tc, jobId, newStepInfo.getNewStepTypeName(), stepCount++,
						Optional.of(stepParameters), newStepInfo.getStepsToRollback());
				} else {
					jobStatus = JobStatus.PROCESSING;
					newStepId = stepDAO.createProcessingStep(tc, jobId, newStepInfo.getNewStepTypeName(), stepCount++,
						Optional.of(stepParameters));
				}
			} else {
				//3.2 Load and update existing step
				StepPOJO step = stepDAO.getStep(tc, jobId, job.getCurrentStepId());
				
				//Set updated step parameters
				for (Map.Entry<String, Parameter> ent : init.getStepParametersUpdate().entrySet())
					step.getStepParameters().put(ent.getKey(), ent.getValue());
				for (String stepParameterToRemove : init.getStepParametersToRemove())
					step.getStepParameters().remove(stepParameterToRemove);
				
				//Update step
				stepDAO.updateStep(tc, job.getCurrentStepId(), step.getExecutionCount(), 
						step.getStepParameters(), step.getProcessingLog());
			}
			
			//4. JoinStatusRecord
			Optional<JoinStatusRecord<Long>> jsr = init.getJoinStatusRecord();
			if (jsr.isPresent()) {
				jobStatus = JobStatus.JOINING;
				job.setJoinStatusRecord(jsr);
			}
			
			this.updateJob(tc, jobId, newStepId, jobStatus, job.getNextExecutionTime(), 
					job.getJobParameters(), stepCount, job.getJoinStatusRecord());
		} finally {
			if (pst != null) pst.close();
		}
	}
	
	public DBJobPOJO loadJob(TransactionContext tc, long jobId) throws SQLException {
		List<Long> jobIds = new ArrayList<>();
		jobIds.add(jobId);
		JobCollection<Long> jobs = loadJobs(tc, Optional.empty(), Optional.empty(), Optional.empty(), 
				Optional.empty(), Optional.of(jobIds), 
				Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
		
		if ((jobs == null) || (jobs.getJobs() == null) || (jobs.getJobs().isEmpty()))
			return null;
		
		for (JobPOJO<Long> job : jobs.getJobs())
			return (DBJobPOJO)job;
		
		return null;
	}
	
	public JobCollection<Long> loadJobs(TransactionContext tc, Optional<Timestamp> from, Optional<Timestamp> to,
			Optional<Timestamp> fromExec, Optional<Timestamp> toExec,
			Optional<Collection<Long>> jobIds, Optional<Collection<Long>> parentJobIds, 
			Optional<Map<String, Long>> jobTypesAndVersions, 
			Optional<Set<String>> currentStepTypes, Optional<PageInfo> pageInfo) throws SQLException {
		Connection connection = ((JDBCTransactionContext)tc).getConnection();
		PreparedStatement pst = null;
		
		try {
			StringBuilder sb = new StringBuilder(
					"SELECT SQL_CALC_FOUND_ROWS j.id_job, j.job_type, j.version, j.job_name, j.parent_job_id," + 
					"		j.current_step_id, j.status, j.next_execution_time, j.job_parameter_state, j.job_initial_parameter_state," + 
					"		j.id_locker, j.step_count, j.wait_for_N_jobs, j.join_parameter_name, " + 
					"	GROUP_CONCAT(r.id_job SEPARATOR ',') AS joined_job_ids, " + 
					"	GROUP_CONCAT(j2.status SEPARATOR ',') AS joined_job_statuses, s.step_type, j.creation_time " + 
					"	FROM steps s, jobs j" + 
					"		LEFT JOIN join_record_jobs r" + 
					"			LEFT JOIN jobs j2" + 
					"			ON j2.id_job = r.id_job" + 
					"		ON j.id_job = r.id_awaiting_job" + 
					"	WHERE j.current_step_id = s.id_step");
			
			if (from.isPresent())
				sb.append(" AND j.creation_time >= ? ");
			if (to.isPresent())
				sb.append(" AND j.creation_time <= ? ");
			if (fromExec.isPresent())
				sb.append(" AND j.next_execution_time >= ? ");
			if (toExec.isPresent())
				sb.append(" AND j.next_execution_time <= ? ");
			if (jobIds.isPresent() && !jobIds.get().isEmpty()) {
				sb.append(" AND j.id_job IN (");
				
				int jobCount = 0;
				for (@SuppressWarnings("unused") long jobId : jobIds.get()) {
					if (jobCount++ > 0) sb.append(", ");
					sb.append("?");
				}
				
				sb.append(")");
			}
			if (parentJobIds.isPresent() && !parentJobIds.get().isEmpty()) {
				sb.append(" AND j.parent_job_id IN (");
				
				int parentJobCount = 0;
				for (@SuppressWarnings("unused") long parentJob : parentJobIds.get()) {
					if (parentJobCount++ > 0) sb.append(", ");
					sb.append("?");
				}
				
				sb.append(")");
			}
			if (jobTypesAndVersions.isPresent() && !jobTypesAndVersions.get().isEmpty()) {
				sb.append(" AND (");
				
				int jobTypeCount = 0;
				for (Map.Entry<String, Long> ent : jobTypesAndVersions.get().entrySet()) {
					if (jobTypeCount++ > 0) sb.append("OR ");
					
					long value = ent.getValue();
					if (value > 0)
						sb.append(" (j.job_type = ? AND j.version = ?) ");
					else
						sb.append(" (j.job_type = ?) ");
				}
				
				sb.append(")");
			}
			if (currentStepTypes.isPresent() && !currentStepTypes.get().isEmpty()) {
				sb.append(" AND s.step_type IN (");
				
				int stepTypeCount = 0;
				for (@SuppressWarnings("unused") String stepType : currentStepTypes.get()) {
					if (stepTypeCount++ > 0) sb.append(", ");
					sb.append("?");
				}
				
				sb.append(")");
			}
			
			sb.append(" GROUP BY j.id_job, j.job_type, j.version, j.job_name, j.parent_job_id," + 
			"		j.current_step_id, j.status, j.next_execution_time, j.job_parameter_state, j.job_initial_parameter_state," + 
			"		j.id_locker, j.step_count, j.wait_for_N_jobs, j.join_parameter_name, s.step_type, j.creation_time" + 
			"		ORDER BY j.creation_time "); 
			
			if (pageInfo.isPresent())
				sb.append("LIMIT ?, ?");

			pst = connection.prepareStatement(sb.toString());
			
			int count = 0;
			if (from.isPresent())
				pst.setLong(++count, ((LongTimestamp)from.get()).getTimeMs());
			if (to.isPresent())
				pst.setLong(++count, ((LongTimestamp)to.get()).getTimeMs());
			if (fromExec.isPresent())
				pst.setLong(++count, ((LongTimestamp)fromExec.get()).getTimeMs());
			if (toExec.isPresent())
				pst.setLong(++count, ((LongTimestamp)toExec.get()).getTimeMs());
			if (jobIds.isPresent() && !jobIds.get().isEmpty())
				for (long jobId : jobIds.get())
					pst.setLong(++count, jobId);
			if (parentJobIds.isPresent())
				for (long parentJobId : parentJobIds.get())
					pst.setLong(++count, parentJobId);
			if (jobTypesAndVersions.isPresent()) {
				for (Map.Entry<String, Long> ent : jobTypesAndVersions.get().entrySet()) {
					String jobType = ent.getKey();
					long version = ent.getValue();
					
					if (version > 0) {
						pst.setString(++count, jobType);
						pst.setLong(++count, version);
					} else
						pst.setString(++count, jobType);
				}
			}
			if (currentStepTypes.isPresent() && !currentStepTypes.get().isEmpty())
				for (String stepType : currentStepTypes.get())
					pst.setString(++count, stepType);
			if (pageInfo.isPresent()) {
				pst.setLong(++count, pageInfo.get().getItemsPerPage()*pageInfo.get().getPageNumber());
				pst.setLong(++count, pageInfo.get().getItemsPerPage());
			}
			ResultSet rs = pst.executeQuery();
			
			List<JobPOJO<Long>> jobColl = new ArrayList<JobPOJO<Long>>();
			while (rs.next())
				jobColl.add(loadJobPOJO(rs));
			
			long foundRows = JDBCTransactionFactory.getFoundRows(connection);
			
			return new JobCollection<Long>(pageInfo, jobColl, foundRows);
		} finally {
			if (pst != null) pst.close();
		}
	}
	
	protected DBJobPOJO loadJobPOJO(ResultSet rs) throws SQLException {
		DBJobPOJO dbJobPOJO;
		long jobId = rs.getLong(1);
		
		String jobTypeName = rs.getString(2);
		long version = rs.getLong(3);
		
		String jobNameStr = rs.getString(4);
		Optional<String> jobName;
		if (rs.wasNull())
			jobName = Optional.empty();
		else
			jobName = Optional.of(jobNameStr);
		
		long parentJobIdLong = rs.getLong(5);
		Optional<Long> parentJobId;
		if (rs.wasNull())
			parentJobId = Optional.empty();
		else
			parentJobId = Optional.of(parentJobIdLong);
		
		long currentStepId = rs.getLong(6);
		
		JobStatus status = JobStatus.fromCode(rs.getInt(7));
		Timestamp nextExecutionTime = ((LongTimeProvider)timeProvider).createTimestamp(rs.getLong(8));
		
		String jobParametersStr = rs.getString(9);
		Map<String, Parameter> jobParameters = 
				parameterContextSerializer.deserializeParameters(new JSONObject(jobParametersStr));
		
		String jobInitialParametersStr = rs.getString(10);
		Map<String, Parameter> jobInitialParameters = 
				parameterContextSerializer.deserializeParameters(new JSONObject(jobInitialParametersStr));
		
		long idLockerL = rs.getLong(11);
		Optional<Long> idLocker = rs.wasNull() ? Optional.empty() : Optional.of(idLockerL);
		
		int stepCount = rs.getInt(12);
		
		Optional<JoinStatusRecord<Long>> joinStatusRecord;
		String joinParameterName = rs.getString(14);
		if (rs.wasNull()) {
			joinStatusRecord = Optional.empty();
		} else {
			int waitForNJobs = rs.getInt(13);
			
			String joinedJobIds = rs.getString(15).trim();
			String joinedJobStatuses = rs.getString(16).trim();

			String[] joinedJobIdParts = joinedJobIds.split(",");
			String[] joinedJobStatusParts = joinedJobStatuses.split(",");
			
			Map<Long, JobStatus> joinedJobs = new HashMap<>();
			for (int i = 0; i < joinedJobIdParts.length; i++) {
				Long joinedJobId = Long.parseLong(joinedJobIdParts[i]);
				JobStatus joinedJobStatus = JobStatus.fromCode(Integer.parseInt(joinedJobStatusParts[i]));
				
				joinedJobs.put(joinedJobId, joinedJobStatus);
			}
			
			JoinStatusRecord<Long> jsr = new MapJoinStatusRecord<Long>(joinedJobs, joinParameterName, 
					waitForNJobs);
			joinStatusRecord = Optional.of(jsr);
		}
		
		String currentStepType = rs.getString(17);
		Timestamp creationTime = ((LongTimeProvider)timeProvider).createTimestamp(rs.getLong(18));
		
		dbJobPOJO = new DBJobPOJO(jobTypeName, version, jobId, jobName, parentJobId, stepCount,
				currentStepType, jobInitialParameters, status, creationTime, nextExecutionTime, 
				jobParameters, joinStatusRecord, currentStepId, idLocker);
		
		return dbJobPOJO;
	}
}
