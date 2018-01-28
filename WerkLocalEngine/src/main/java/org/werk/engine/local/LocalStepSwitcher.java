package org.werk.engine.local;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.werk.engine.StepSwitchResult;
import org.werk.engine.SwitchStatus;
import org.werk.engine.WerkStepSwitcher;
import org.werk.engine.processing.WerkJob;
import org.werk.engine.processing.WerkStep;
import org.werk.processing.jobs.Job;
import org.werk.processing.jobs.JobStatus;
import org.werk.processing.steps.ExecutionResult;
import org.werk.processing.steps.Step;
import org.werk.processing.steps.Transition;
import org.werk.processing.steps.TransitionStatus;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class LocalStepSwitcher<J> implements WerkStepSwitcher<J> {
	final Logger logger = LoggerFactory.getLogger(LocalStepSwitcher.class);
	
	protected LocalJobStepFactory<J> jobStepFactory;
	protected LocalJobManager<J> jobManager;
	
	@Override
	public StepSwitchResult redo(Job<J> job, ExecutionResult<J> exec) {
		return new StepSwitchResult(SwitchStatus.PROCESS, exec.getDelayMS());
	}

	@Override
	public StepSwitchResult join(Job<J> job, ExecutionResult<J> exec) {
		try {
			jobManager.join(((LocalWerkJob<J>)job).getJobId(), exec.getJobsToJoin().get());
		} catch(Exception ex) {
			logger.error("JobManager's \"join\" callback failed", ex);
		}
		
		return new StepSwitchResult(SwitchStatus.UNLOAD);
	}

	@Override
	public StepSwitchResult transition(Job<J> job, Transition transition) {
		try {
			if (transition.getTransitionStatus() == TransitionStatus.NEXT_STEP) {
				job.setStatus(JobStatus.PROCESSING);
				
				long stepNumber = ((LocalWerkJob<J>)job).getNextStepNumber();
				String stepName = transition.getStepName().get();
				Step<J> nextStep = jobStepFactory.createNewStep(job, stepNumber, stepName);
				
				currentStepDone(job);
				
				((WerkJob<J>)job).setCurrentStep((WerkStep<J>)nextStep);
				
				return new StepSwitchResult(SwitchStatus.PROCESS); 
			} else if (transition.getTransitionStatus() == TransitionStatus.ROLLBACK) {
				job.setStatus(JobStatus.ROLLING_BACK);
				
				long stepNumber = ((LocalWerkJob<J>)job).getNextStepNumber();
				String stepName = transition.getStepName().get();
				
				Step<J> nextStep = jobStepFactory.createNewStep(job, stepNumber, transition.getRollbackStepNumbers(), stepName);
				
				currentStepDone(job);
				
				((WerkJob<J>)job).setCurrentStep((WerkStep<J>)nextStep);
				
				return new StepSwitchResult(SwitchStatus.PROCESS);
			} else if (transition.getTransitionStatus() == TransitionStatus.FINISH) {
				currentStepDone(job);
				job.setStatus(JobStatus.FINISHED);
				
				try {
					jobManager.jobFinished(((LocalWerkJob<J>)job).getJobId());
				} catch(Exception ex) {
					logger.error("JobManager's \"jobFailed\" callback failed", ex);
					throw ex;
				}
				
				return new StepSwitchResult(SwitchStatus.UNLOAD);
			} else if (transition.getTransitionStatus() == TransitionStatus.FAIL) {
				currentStepDone(job);
				job.setStatus(JobStatus.FAILED);
				
				try {
					jobManager.jobFailed(((LocalWerkJob<J>)job).getJobId());
				} catch(Exception ex) {
					logger.error("JobManager's \"jobFailed\" callback failed", ex);
					throw ex;
				}
				
				return new StepSwitchResult(SwitchStatus.UNLOAD);
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
		job.getCurrentStep().appendToProcessingLog(
				String.format("StepExec error [%s]", e)
			);
		job.setStatus(JobStatus.FAILED);
		
		currentStepDone(job);
		
		try {
			jobManager.jobFailed(((LocalWerkJob<J>)job).getJobId());
		} catch(Exception ex) {
			logger.error("JobManager's \"jobFailed\" callback failed", ex);
		}
		
		return new StepSwitchResult(SwitchStatus.UNLOAD);
	}

	@Override
	public StepSwitchResult stepTransitionError(Job<J> job, Exception e) {
		job.getCurrentStep().appendToProcessingLog(
				String.format("Transitioner error [%s]", e)
			);
		job.setStatus(JobStatus.FAILED);
		
		currentStepDone(job);
		
		try {
			jobManager.jobFailed(((LocalWerkJob<J>)job).getJobId());
		} catch(Exception ex) {
			logger.error("JobManager's \"jobFailed\" callback failed", ex);
		}
		
		return new StepSwitchResult(SwitchStatus.UNLOAD);
	}
	
	protected void currentStepDone(Job<J> job) {
		LocalWerkJob<J> lwj = (LocalWerkJob<J>)job;
		
		Step<J> currentStep = lwj.getCurrentStep();
		lwj.setCurrentStep(null);
		
		lwj.processingHistory.add(currentStep);
	}
}
