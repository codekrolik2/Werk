package org.werk.engine.sql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.pillar.db.interfaces.TransactionContext;
import org.pillar.time.interfaces.TimeProvider;
import org.pillar.time.interfaces.Timestamp;
import org.werk.config.WerkConfig;
import org.werk.engine.StepSwitchResult;
import org.werk.engine.WerkStepSwitcher;
import org.werk.engine.sql.DAO.JobDAO;
import org.werk.engine.sql.DAO.StepDAO;
import org.werk.exceptions.StepLogLimitExceededException;
import org.werk.meta.StepType;
import org.werk.processing.jobs.Job;
import org.werk.processing.jobs.JobStatus;
import org.werk.processing.jobs.JoinStatusRecord;
import org.werk.processing.jobs.JoinStatusRecordImpl;
import org.werk.processing.steps.ExecutionResult;
import org.werk.processing.steps.StepExec;
import org.werk.processing.steps.Transition;
import org.werk.processing.steps.TransitionStatus;
import org.werk.processing.steps.Transitioner;

public class SQLStepSwitcher implements WerkStepSwitcher<Long> {
	final Logger logger = Logger.getLogger(SQLStepSwitcher.class);
	
	protected TimeProvider timeProvider;
	protected JobDAO jobDAO;
	protected StepDAO stepDAO;
	protected WerkConfig<Long> werkConfig; 
	
	public SQLStepSwitcher(TimeProvider timeProvider, JobDAO jobDAO, StepDAO stepDAO, WerkConfig<Long> werkConfig) {
		this.timeProvider = timeProvider;
		this.jobDAO = jobDAO;
		this.stepDAO = stepDAO;
		this.werkConfig = werkConfig;
	}
	
	protected void updateCurrentStep(TransactionContext tc, SQLWerkJob sqlJob) throws Exception {
		SQLWerkStep step = (SQLWerkStep)sqlJob.getCurrentStep();
		long stepId = step.getStepId();
		
		stepDAO.updateStep(tc, stepId, step.getExecutionCount(), step.getStepParameters(), 
				step.getProcessingLog());
	}
	
	protected void updateJob(TransactionContext tc, SQLWerkJob sqlJob) throws Exception {
		long stepId = ((SQLWerkStep)sqlJob.getCurrentStep()).getStepId();
		jobDAO.updateJob(tc, sqlJob.getJobId(), stepId, sqlJob.getStatus(), sqlJob.getNextExecutionTime(),
				sqlJob.getJobParameters(), sqlJob.getStepCount(), sqlJob.getJoinStatusRecord());
	}

	public TransactionContext update(SQLWerkJob sqlJob) throws Exception {
		TransactionContext tc = sqlJob.getOrCreateTC();
		updateJob(tc, sqlJob);
		updateCurrentStep(tc, sqlJob);
		return tc;
	}
	
	@Override
	public StepSwitchResult redo(Job<Long> job, ExecutionResult<Long> exec) {
		SQLWerkJob sqlJob = (SQLWerkJob)job;
		
		TransactionContext tc = null;
		try {
			Timestamp nextExecutionTime = timeProvider.getCurrentTime();
			if (exec.getDelayMS().isPresent())
				nextExecutionTime.shiftBy(exec.getDelayMS().get(), TimeUnit.MILLISECONDS);
			
			sqlJob.setNextExecutionTime(nextExecutionTime);
			
			update(sqlJob);
		} catch(Exception e) {
			logger.error(
				String.format("Error processing redo. Unloading job [%d]. Losing heartbeat.", job.getJobId()),
				e);
			
			//TODO: unload lose HB
		} finally {
			sqlJob.closeTCIfNeeded(tc);
		}
		
		return StepSwitchResult.process(exec.getDelayMS());
	}
	
	@Override
	public StepSwitchResult callback(Job<Long> job, ExecutionResult<Long> exec) {
		SQLWerkJob sqlJob = (SQLWerkJob)job;
		
		TransactionContext tc = null;
		try {
			Timestamp nextExecutionTime = timeProvider.getCurrentTime();
			sqlJob.setNextExecutionTime(nextExecutionTime);
			
			tc = update(sqlJob);
		} catch(Exception e) {
			logger.error(
				String.format("Error processing callback. Unloading job [%d]. Losing heartbeat.", job.getJobId()), 
				e);
			
			//TODO: unload lose HB
		} finally {
			if (sqlJob.getStepTransactionContext() != null)
				try { tc.close(); } catch (Exception e) { logger.error("Transaction close error", e); }
		}
		
		return StepSwitchResult.callback(exec.getCallback().get(), exec.getDelayMS(), exec.getParameterName().get());
	}
	
	@Override
	public StepSwitchResult join(Job<Long> job, ExecutionResult<Long> exec) {
		SQLWerkJob sqlJob = (SQLWerkJob)job;
		
		TransactionContext tc = null;
		try {
			List<Long> joinedJobs = exec.getJobsToJoin().get();
			String joinParameterName = exec.getParameterName().get();
			JobStatus statusBeforeJoin = job.getStatus();
			Optional<Long> waitForNJobs = exec.getWaitForNJobs();
			
			JoinStatusRecord<Long> joinStatusRecord = new JoinStatusRecordImpl<Long>(joinedJobs, 
					joinParameterName, statusBeforeJoin, waitForNJobs);
			
			sqlJob.setJoinStatusRecord(Optional.of(joinStatusRecord));
			sqlJob.setStatus(JobStatus.JOINING);
			
			Timestamp nextExecutionTime = timeProvider.getCurrentTime();
			sqlJob.setNextExecutionTime(nextExecutionTime);
			
			tc = update(sqlJob);
		} catch(Exception e) {
			logger.error(
				String.format("Error processing join. Unloading job [%d]. Losing heartbeat.", job.getJobId()), 
				e);
			
			//TODO: unload lose HB
		} finally {
			if (sqlJob.getStepTransactionContext() != null)
				try { tc.close(); } catch (Exception e) { logger.error("Transaction close error", e); }
		}
		
		return StepSwitchResult.unload();
	}
	
	@Override
	public StepSwitchResult transition(Job<Long> job, Transition transition) {
		SQLWerkJob sqlJob = (SQLWerkJob)job;
		
		TransactionContext tc = null;
		try {
			if ((transition.getTransitionStatus() == TransitionStatus.NEXT_STEP) || 
				(transition.getTransitionStatus() == TransitionStatus.ROLLBACK)) {
				//TODO: Check for processing history limit
				//TODO: Remove first history record if applicable
			}
			
			if (transition.getTransitionStatus() == TransitionStatus.NEXT_STEP) {
				//TODO: check that transition is allowed
				
				tc = sqlJob.getOrCreateTC();
				//Save current step update
				updateCurrentStep(tc, sqlJob);
				
				//create a new step and set it as current for a job
				int stepNumber = sqlJob.getNextStepNumber();
				String stepTypeName = transition.getStepTypeName().get();
				
				StepType<Long> stepType = werkConfig.getStepType(stepTypeName);
				StepExec<Long> stepExec = werkConfig.getStepExec(stepTypeName);
				Transitioner<Long> stepTransitioner = werkConfig.getStepTransitioner(stepTypeName); 
				
				long nextStepId = stepDAO.createProcessingStep(tc, job.getJobId(), stepTypeName, stepNumber);
				SQLWerkStep newStep = new SQLWerkStep(sqlJob, stepType, false, stepNumber, new ArrayList<>(),
						0, new HashMap<>(), new ArrayList<>(), stepExec, stepTransitioner, timeProvider, nextStepId);
				
				//save job update
				Timestamp nextExecutionTime = timeProvider.getCurrentTime();
				if (transition.getDelayMS().isPresent())
					nextExecutionTime.shiftBy(transition.getDelayMS().get(), TimeUnit.MILLISECONDS);
				sqlJob.setNextExecutionTime(nextExecutionTime);
				sqlJob.setStatus(JobStatus.PROCESSING);
				sqlJob.setCurrentStep(newStep);
				
				updateJob(tc, sqlJob);
				
				return StepSwitchResult.process();
			} else if (transition.getTransitionStatus() == TransitionStatus.ROLLBACK) {
				//TODO: check that transition is allowed
				
				tc = sqlJob.getOrCreateTC();
				
				//Save current step update
				updateCurrentStep(tc, sqlJob);
				
				//create a new step and set it as current for a job
				int stepNumber = sqlJob.getNextStepNumber();
				String stepTypeName = transition.getStepTypeName().get();
				
				StepType<Long> stepType = werkConfig.getStepType(stepTypeName);
				StepExec<Long> stepExec = werkConfig.getStepExec(stepTypeName);
				Transitioner<Long> stepTransitioner = werkConfig.getStepTransitioner(stepTypeName); 
				
				long nextStepId = stepDAO.createRollbackStep(tc, job.getJobId(), stepTypeName, stepNumber, 
						transition.getRollbackStepParameters(), transition.getRollbackStepNumbers());
				SQLWerkStep newStep = new SQLWerkStep(sqlJob, stepType, false, stepNumber, transition.getRollbackStepNumbers(),
						0, transition.getRollbackStepParameters(), new ArrayList<>(), stepExec, stepTransitioner, 
						timeProvider, nextStepId);
				
				//save job update
				Timestamp nextExecutionTime = timeProvider.getCurrentTime();
				if (transition.getDelayMS().isPresent())
					nextExecutionTime.shiftBy(transition.getDelayMS().get(), TimeUnit.MILLISECONDS);
				sqlJob.setNextExecutionTime(nextExecutionTime);
				sqlJob.setStatus(JobStatus.ROLLING_BACK);
				sqlJob.setCurrentStep(newStep);
				
				updateJob(tc, sqlJob);
				
				return StepSwitchResult.process();
			} else if (transition.getTransitionStatus() == TransitionStatus.FINISH) {
				Timestamp nextExecutionTime = timeProvider.getCurrentTime();
				sqlJob.setNextExecutionTime(nextExecutionTime);
				
				((SQLWerkJob)job).setStatus(JobStatus.FINISHED);
				
				tc = update(sqlJob);
				
				return StepSwitchResult.unload();
			} else if (transition.getTransitionStatus() == TransitionStatus.FINISH_ROLLBACK) {
				Timestamp nextExecutionTime = timeProvider.getCurrentTime();
				sqlJob.setNextExecutionTime(nextExecutionTime);
				
				((SQLWerkJob)job).setStatus(JobStatus.ROLLED_BACK);
				
				tc = update(sqlJob);
				
				return StepSwitchResult.unload();
			} else if (transition.getTransitionStatus() == TransitionStatus.FAIL) {
				Timestamp nextExecutionTime = timeProvider.getCurrentTime();
				sqlJob.setNextExecutionTime(nextExecutionTime);
				
				((SQLWerkJob)job).setStatus(JobStatus.FAILED);
				
				tc = update(sqlJob);
				
				return StepSwitchResult.unload();
			} else
				return stepTransitionError(job, new Exception(
						String.format("Unknown transition type: [%s]", transition.getTransitionStatus())
					)
				);
		} catch(Exception e) {
			logger.error(
				String.format("Error processing transition. Unloading job [%d]. Losing heartbeat.", job.getJobId()), 
				e);
			
			//TODO: unload lose HB
			
			return StepSwitchResult.unload();
		} finally {
			if (sqlJob.getStepTransactionContext() != null)
				try { tc.close(); } catch (Exception e) { logger.error("Transaction close error", e); }
		}
	}
	
	@Override
	public StepSwitchResult stepExecError(Job<Long> job, Exception e) {
		try {
			job.getCurrentStep().appendToProcessingLog(
					String.format("StepExec error [%s]", e)
				);
		} catch (StepLogLimitExceededException e1) {
			logger.error(
				String.format("Failed to append exec error message to step log: log limit exceeded. Message: [%s]",
					String.format("StepExec error [%s]", e)
				), e
			);
		}
		
		SQLWerkJob sqlJob = (SQLWerkJob)job;
		
		TransactionContext tc = null;
		try {
			Timestamp nextExecutionTime = timeProvider.getCurrentTime();
			sqlJob.setNextExecutionTime(nextExecutionTime);
			
			((SQLWerkJob)job).setStatus(JobStatus.FAILED);
			
			tc = update(sqlJob);
		} catch(Exception e1) {
			logger.error(
				String.format("Error processing stepExecError. Unloading job [%d]. Losing heartbeat.", job.getJobId()), 
				e1);
			
			//TODO: unload lose HB
		} finally {
			if (sqlJob.getStepTransactionContext() != null)
				try { tc.close(); } catch (Exception e1) { logger.error("Transaction close error", e1); }
		}
		
		return StepSwitchResult.unload();
	}
	
	@Override
	public StepSwitchResult stepTransitionError(Job<Long> job, Exception e) {
		try {
			job.getCurrentStep().appendToProcessingLog(
					String.format("Transitioner error [%s]", e)
				);
		} catch (StepLogLimitExceededException e1) {
			logger.error(
				String.format("Failed to append transitioner error message to step log: log limit exceeded. Message: [%s]",
					String.format("Transitioner error [%s]", e)
				), e
			);
		}
		
		SQLWerkJob sqlJob = (SQLWerkJob)job;
		
		TransactionContext tc = null;
		try {
			Timestamp nextExecutionTime = timeProvider.getCurrentTime();
			sqlJob.setNextExecutionTime(nextExecutionTime);
			
			((SQLWerkJob)job).setStatus(JobStatus.FAILED);
			
			tc = update(sqlJob);
		} catch(Exception e1) {
			logger.error(
				String.format("Error processing stepTransitionError. Unloading job [%d]. Losing heartbeat.", job.getJobId()), 
				e1);
			
			//TODO: unload lose HB
		} finally {
			if (sqlJob.getStepTransactionContext() != null)
				try { tc.close(); } catch (Exception e1) { logger.error("Transaction close error", e1); }
		}
		
		return StepSwitchResult.unload();
	}
}
