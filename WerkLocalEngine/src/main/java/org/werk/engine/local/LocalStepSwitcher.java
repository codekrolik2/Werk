package org.werk.engine.local;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.werk.data.StepPOJO;
import org.werk.engine.StepSwitchResult;
import org.werk.engine.WerkStepSwitcher;
import org.werk.engine.processing.WerkJob;
import org.werk.engine.processing.WerkStep;
import org.werk.exceptions.StepLogLimitExceededException;
import org.werk.exceptions.WerkException;
import org.werk.meta.OverflowAction;
import org.werk.processing.jobs.Job;
import org.werk.processing.jobs.JobStatus;
import org.werk.processing.jobs.JoinStatusRecord;
import org.werk.processing.steps.ExecutionResult;
import org.werk.processing.steps.Step;
import org.werk.processing.steps.Transition;
import org.werk.processing.steps.TransitionStatus;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class LocalStepSwitcher<J> implements WerkStepSwitcher<J> {
	final Logger logger = Logger.getLogger(LocalStepSwitcher.class);
	
	protected LocalJobStepFactory<J> jobStepFactory;
	protected LocalJobManager<J> jobManager;
	
	@Override
	public StepSwitchResult redo(Job<J> job, ExecutionResult<J> exec) {
		return StepSwitchResult.process(exec.getDelayMS());
	}

	@Override
	public StepSwitchResult callback(Job<J> job, ExecutionResult<J> exec) {
		return StepSwitchResult.callback(exec.getCallback().get(), exec.getDelayMS(), exec.getParameterName().get());
	}
	
	@Override
	public StepSwitchResult join(Job<J> job, ExecutionResult<J> exec) {
		try {
			List<J> joinedJobs = exec.getJobsToJoin().get();
			String joinParameterName = exec.getParameterName().get();
			JobStatus statusBeforeJoin = job.getStatus();
			int waitForNJobs = exec.getWaitForNJobs().get();
			
			JoinStatusRecord<J> joinStatusRecord = new LocalJoinStatusRecord<J>(new HashSet<>(joinedJobs), 
					joinParameterName, statusBeforeJoin, waitForNJobs, jobManager);
			
			((LocalWerkJob<J>)job).setJoinStatusRecord(Optional.of(joinStatusRecord));
			((LocalWerkJob<J>)job).setStatus(JobStatus.JOINING);
			
			jobManager.join(((LocalWerkJob<J>)job).getJobId(), exec.getJobsToJoin().get());
		} catch(Exception ex) {
			logger.error("JobManager's \"join\" callback failed", ex);
		}
		
		return StepSwitchResult.unload();
	}

	@Override
	public StepSwitchResult transition(Job<J> job, Transition transition) {
		try {
			if ((transition.getTransitionStatus() == TransitionStatus.NEXT_STEP) ||
				(transition.getTransitionStatus() == TransitionStatus.ROLLBACK)) {
				long historyLimit = ((WerkJob<J>)job).getJobType().getHistoryLimit();
				if (job.getCurrentStep().getStepNumber() >= historyLimit) {
					if (((WerkJob<J>)job).getJobType().getHistoryOverflowAction() == OverflowAction.FAIL) {
						return stepTransitionError(job,
							new WerkException(
									String.format("Job History Limit reached [%d]", ((WerkJob<J>)job).getJobType().getHistoryLimit())
								));
					} else {
						List<StepPOJO> history = job.getProcessingHistory().stream().skip(1).collect(Collectors.toList());
						((LocalWerkJob<J>)job).setProcessingHistory(history);
					}
				}
			}
			
			if (transition.getTransitionStatus() == TransitionStatus.NEXT_STEP) {
				((LocalWerkJob<J>)job).setStatus(JobStatus.PROCESSING);
				
				int stepNumber = ((LocalWerkJob<J>)job).getNextStepNumber();
				String stepTypeName = transition.getStepTypeName().get();
				Step<J> nextStep = jobStepFactory.createNewStep(job, stepNumber, stepTypeName);
				
				currentStepDone(job);
				
				((WerkJob<J>)job).setCurrentStep((WerkStep<J>)nextStep);
				
				return StepSwitchResult.process();
			} else if (transition.getTransitionStatus() == TransitionStatus.ROLLBACK) {
				((LocalWerkJob<J>)job).setStatus(JobStatus.ROLLING_BACK);
				
				int stepNumber = ((LocalWerkJob<J>)job).getNextStepNumber();
				String stepTypeName = transition.getStepTypeName().get();
				
				Step<J> nextStep = jobStepFactory.createNewStep(job, stepNumber, transition.getRollbackStepNumbers(), stepTypeName);
				
				currentStepDone(job);
				
				((WerkJob<J>)job).setCurrentStep((WerkStep<J>)nextStep);
				
				return StepSwitchResult.process();
			} else if (transition.getTransitionStatus() == TransitionStatus.FINISH) {
				currentStepDone(job);
				((LocalWerkJob<J>)job).setStatus(JobStatus.FINISHED);
				
				try {
					jobManager.jobFinished(((LocalWerkJob<J>)job).getJobId());
				} catch(Exception ex) {
					logger.error("JobManager's \"jobFailed\" callback failed", ex);
					throw ex;
				}
				
				return StepSwitchResult.unload();
			} else if (transition.getTransitionStatus() == TransitionStatus.FINISH_ROLLBACK) {
				currentStepDone(job);
				((LocalWerkJob<J>)job).setStatus(JobStatus.ROLLED_BACK);
				
				try {
					jobManager.jobFinished(((LocalWerkJob<J>)job).getJobId());
				} catch(Exception ex) {
					logger.error("JobManager's \"jobFailed\" callback failed", ex);
					throw ex;
				}
				
				return StepSwitchResult.unload();
			} else if (transition.getTransitionStatus() == TransitionStatus.FAIL) {
				currentStepDone(job);
				((LocalWerkJob<J>)job).setStatus(JobStatus.FAILED);
				
				try {
					jobManager.jobFailed(((LocalWerkJob<J>)job).getJobId());
				} catch(Exception ex) {
					logger.error("JobManager's \"jobFailed\" callback failed", ex);
					throw ex;
				}
				
				return StepSwitchResult.unload();
			} else 
				throw new Exception(
						String.format("Unknown transition type: [%s]", transition.getTransitionStatus())
					);
		} catch (Exception e) {
			//Fail job
			return stepTransitionError(job, e);
		}
	}

	@Override
	public StepSwitchResult stepExecError(Job<J> job, Exception e) {
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
		((LocalWerkJob<J>)job).setStatus(JobStatus.FAILED);
		
		currentStepDone(job);
		
		try {
			jobManager.jobFailed(((LocalWerkJob<J>)job).getJobId());
		} catch(Exception ex) {
			logger.error("JobManager's \"jobFailed\" callback failed", ex);
		}
		
		return StepSwitchResult.unload();
	}

	@Override
	public StepSwitchResult stepTransitionError(Job<J> job, Exception e) {
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
		((LocalWerkJob<J>)job).setStatus(JobStatus.FAILED);
		
		currentStepDone(job);
		
		try {
			jobManager.jobFailed(((LocalWerkJob<J>)job).getJobId());
		} catch(Exception ex) {
			logger.error("JobManager's \"jobFailed\" callback failed", ex);
		}
		
		return StepSwitchResult.unload();
	}
	
	protected void currentStepDone(Job<J> job) {
		LocalWerkJob<J> lwj = (LocalWerkJob<J>)job;
		
		Step<J> currentStep = lwj.getCurrentStep();
		lwj.setCurrentStep(null);
		
		lwj.processingHistory.add(currentStep);
	}
}
