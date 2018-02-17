package org.werk.engine.sql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.werk.engine.sql.exception.SQLStepSwitcherException;
import org.werk.exceptions.StepLogLimitExceededException;
import org.werk.meta.StepType;
import org.werk.processing.jobs.Job;
import org.werk.processing.jobs.JobStatus;
import org.werk.processing.jobs.JoinStatusRecord;
import org.werk.processing.jobs.MapJoinStatusRecord;
import org.werk.processing.parameters.Parameter;
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
	
	protected void updateCurrentStep(SQLWerkJob sqlJob) throws Exception {
		TransactionContext tc = sqlJob.getStepTransactionContext();
		
		SQLWerkStep step = (SQLWerkStep)sqlJob.getCurrentStep();
		long stepId = step.getStepId();
		
		stepDAO.updateStep(tc, stepId, step.getExecutionCount(), step.getStepParameters(), 
				step.getProcessingLog());
	}
	
	protected void updateJob(SQLWerkJob sqlJob) throws Exception {
		TransactionContext tc = sqlJob.getStepTransactionContext();
		
		long stepId = ((SQLWerkStep)sqlJob.getCurrentStep()).getStepId();
		jobDAO.updateJob(tc, sqlJob.getJobId(), stepId, sqlJob.getStatus(), sqlJob.getNextExecutionTime(),
				sqlJob.getJobParameters(), sqlJob.getStepCount(), sqlJob.getJoinStatusRecord());
	}

	public void updateJobAndCurrentStep(SQLWerkJob sqlJob) throws Exception {
		updateJob(sqlJob);
		updateCurrentStep(sqlJob);
	}
	
	@Override
	public StepSwitchResult redo(Job<Long> job, ExecutionResult<Long> exec) {
		SQLWerkJob sqlJob = (SQLWerkJob)job;
		
		try {
			Timestamp nextExecutionTime = timeProvider.getCurrentTime();
			if (exec.getDelayMS().isPresent())
				nextExecutionTime.shiftBy(exec.getDelayMS().get(), TimeUnit.MILLISECONDS);
			
			sqlJob.setNextExecutionTime(nextExecutionTime);
			
			updateJobAndCurrentStep(sqlJob);
		} catch(Exception e) {
			logger.error(
				String.format("Error processing redo. Unloading job [%d]. Losing heartbeat.", job.getJobId()),
				e);
			
			throw new SQLStepSwitcherException(e);
		}
		
		return StepSwitchResult.process(exec.getDelayMS());
	}
	
	@Override
	public StepSwitchResult callback(Job<Long> job, ExecutionResult<Long> exec) {
		SQLWerkJob sqlJob = (SQLWerkJob)job;
		
		try {
			Timestamp nextExecutionTime = timeProvider.getCurrentTime();
			sqlJob.setNextExecutionTime(nextExecutionTime);
			
			updateJobAndCurrentStep(sqlJob);
		} catch(Exception e) {
			logger.error(
				String.format("Error processing callback. Unloading job [%d]. Losing heartbeat.", job.getJobId()), 
				e);
			
			throw new SQLStepSwitcherException(e);
		}
		
		return StepSwitchResult.callback(exec.getCallback().get(), exec.getDelayMS(), exec.getParameterName().get());
	}
	
	@Override
	public StepSwitchResult join(Job<Long> job, ExecutionResult<Long> exec) {
		SQLWerkJob sqlJob = (SQLWerkJob)job;
		
		try {
			Map<Long, JobStatus> joinedJobs = new HashMap<>();
			for (long jobId : exec.getJobsToJoin().get())
				joinedJobs.put(jobId, JobStatus.UNDEFINED);
			
			String joinParameterName = exec.getParameterName().get();
			Optional<Integer> waitForNJobs = exec.getWaitForNJobs();
			
			JoinStatusRecord<Long> joinStatusRecord = new MapJoinStatusRecord<Long>(joinedJobs, 
					joinParameterName, waitForNJobs.isPresent() ? waitForNJobs.get() : joinedJobs.size());
			
			sqlJob.setJoinStatusRecord(Optional.of(joinStatusRecord));
			sqlJob.setStatus(JobStatus.JOINING);
			
			Timestamp nextExecutionTime = timeProvider.getCurrentTime();
			sqlJob.setNextExecutionTime(nextExecutionTime);
			
			updateJobAndCurrentStep(sqlJob);
		} catch(Exception e) {
			logger.error(
				String.format("Error processing join. Unloading job [%d]. Losing heartbeat.", job.getJobId()), 
				e);
			
			throw new SQLStepSwitcherException(e);
		}
		
		return StepSwitchResult.unload();
	}
	
	@Override
	public StepSwitchResult transition(Job<Long> job, Transition transition) {
		SQLWerkJob sqlJob = (SQLWerkJob)job;
		
		try {
			if ((transition.getTransitionStatus() == TransitionStatus.NEXT_STEP) || 
				(transition.getTransitionStatus() == TransitionStatus.ROLLBACK)) {
				//TODO: Check for processing history limit
				//TODO: Remove first history record if applicable
			}
			
			if (transition.getTransitionStatus() == TransitionStatus.NEXT_STEP) {
				//TODO: check that transition is allowed
				
				//Save current step update
				updateCurrentStep(sqlJob);
				
				//create a new step and set it as current for a job
				int stepNumber = sqlJob.getNextStepNumber();
				String stepTypeName = transition.getStepTypeName().get();
				
				StepType<Long> stepType = werkConfig.getStepType(stepTypeName);
				StepExec<Long> stepExec = werkConfig.getStepExec(stepTypeName);
				Transitioner<Long> stepTransitioner = werkConfig.getStepTransitioner(stepTypeName); 
				
				long nextStepId = stepDAO.createProcessingStep(((SQLWerkJob)job).getStepTransactionContext(), 
						job.getJobId(), stepTypeName, stepNumber);
				SQLWerkStep newStep = new SQLWerkStep(sqlJob, stepType, false, stepNumber, new ArrayList<>(),
						0, new HashMap<>(), new ArrayList<>(), stepExec, stepTransitioner, timeProvider, nextStepId);
				
				//save job update
				Timestamp nextExecutionTime = timeProvider.getCurrentTime();
				if (transition.getDelayMS().isPresent())
					nextExecutionTime.shiftBy(transition.getDelayMS().get(), TimeUnit.MILLISECONDS);
				sqlJob.setNextExecutionTime(nextExecutionTime);
				sqlJob.setStatus(JobStatus.PROCESSING);
				sqlJob.setCurrentStep(newStep);
				
				updateJob(sqlJob);
				
				return StepSwitchResult.process();
			} else if (transition.getTransitionStatus() == TransitionStatus.ROLLBACK) {
				//TODO: check that rollback transition is allowed
				
				sqlJob.getOrCreateTC();
				
				//Save current step update
				updateCurrentStep(sqlJob);
				
				//create a new step and set it as current for a job
				int stepNumber = sqlJob.getNextStepNumber();
				String stepTypeName = transition.getStepTypeName().get();
				
				StepType<Long> stepType = werkConfig.getStepType(stepTypeName);
				StepExec<Long> stepExec = werkConfig.getStepExec(stepTypeName);
				Transitioner<Long> stepTransitioner = werkConfig.getStepTransitioner(stepTypeName); 
				
				long nextStepId = stepDAO.createRollbackStep(((SQLWerkJob)job).getStepTransactionContext(), 
						job.getJobId(), stepTypeName, stepNumber, 
						transition.getRollbackStepParameters(), transition.getRollbackStepNumbers());
				
				List<Integer> rollbackStepNumbers;
				if (transition.getRollbackStepNumbers().isPresent())
					rollbackStepNumbers = transition.getRollbackStepNumbers().get();
				else
					rollbackStepNumbers = new ArrayList<>();
				
				Map<String, Parameter> rollbackStepParameters;
				if (transition.getRollbackStepParameters().isPresent())
					rollbackStepParameters = transition.getRollbackStepParameters().get();
				else
					rollbackStepParameters = new HashMap<>();
				
				SQLWerkStep newStep = new SQLWerkStep(sqlJob, stepType, true, stepNumber, rollbackStepNumbers,
						0, rollbackStepParameters, new ArrayList<>(), stepExec, stepTransitioner, 
						timeProvider, nextStepId);
				
				//save job update
				Timestamp nextExecutionTime = timeProvider.getCurrentTime();
				if (transition.getDelayMS().isPresent())
					nextExecutionTime.shiftBy(transition.getDelayMS().get(), TimeUnit.MILLISECONDS);
				sqlJob.setNextExecutionTime(nextExecutionTime);
				sqlJob.setStatus(JobStatus.ROLLING_BACK);
				sqlJob.setCurrentStep(newStep);
				
				updateJob(sqlJob);
				
				return StepSwitchResult.process();
			} else if (transition.getTransitionStatus() == TransitionStatus.FINISH) {
				Timestamp nextExecutionTime = timeProvider.getCurrentTime();
				sqlJob.setNextExecutionTime(nextExecutionTime);
				
				((SQLWerkJob)job).setStatus(JobStatus.FINISHED);
				
				updateJobAndCurrentStep(sqlJob);
				
				return StepSwitchResult.unload();
			} else if (transition.getTransitionStatus() == TransitionStatus.FINISH_ROLLBACK) {
				Timestamp nextExecutionTime = timeProvider.getCurrentTime();
				sqlJob.setNextExecutionTime(nextExecutionTime);
				
				((SQLWerkJob)job).setStatus(JobStatus.ROLLED_BACK);
				
				updateJobAndCurrentStep(sqlJob);
				
				return StepSwitchResult.unload();
			} else if (transition.getTransitionStatus() == TransitionStatus.FAIL) {
				Timestamp nextExecutionTime = timeProvider.getCurrentTime();
				sqlJob.setNextExecutionTime(nextExecutionTime);
				
				((SQLWerkJob)job).setStatus(JobStatus.FAILED);
				
				updateJobAndCurrentStep(sqlJob);
				
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
			
			throw new SQLStepSwitcherException(e);
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
		
		try {
			Timestamp nextExecutionTime = timeProvider.getCurrentTime();
			sqlJob.setNextExecutionTime(nextExecutionTime);
			
			((SQLWerkJob)job).setStatus(JobStatus.FAILED);
			
			updateJobAndCurrentStep(sqlJob);
		} catch(Exception e1) {
			logger.error(
				String.format("Error processing stepExecError. Unloading job [%d]. Losing heartbeat.", job.getJobId()), 
				e1);
			
			throw new SQLStepSwitcherException(e);
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
				String.format("Failed to append transitioner error message to step log: log limit exceeded. ErrorMessage: [%s]",
					String.format("Transitioner error [%s]", e)
				), e
			);
		}
		
		SQLWerkJob sqlJob = (SQLWerkJob)job;
		
		try {
			Timestamp nextExecutionTime = timeProvider.getCurrentTime();
			sqlJob.setNextExecutionTime(nextExecutionTime);
			
			((SQLWerkJob)job).setStatus(JobStatus.FAILED);
			
			updateJobAndCurrentStep(sqlJob);
		} catch(Exception e1) {
			logger.error(
				String.format("Error processing stepTransitionError. Unloading job [%d]. Losing heartbeat.", job.getJobId()), 
				e1);
			
			throw new SQLStepSwitcherException(e);
		}
		
		return StepSwitchResult.unload();
	}
}
