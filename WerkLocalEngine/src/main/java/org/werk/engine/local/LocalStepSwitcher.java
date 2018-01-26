package org.werk.engine.local;

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

public class LocalStepSwitcher implements WerkStepSwitcher {
	protected LocalJobStepFactory jobStepFactory;
	
	public LocalStepSwitcher(LocalJobStepFactory jobStepFactory) {
		this.jobStepFactory = jobStepFactory;
	}
	
	@Override
	public StepSwitchResult redo(Job job, ExecutionResult exec) {
		return new StepSwitchResult(SwitchStatus.PROCESS, exec.getDelayMS());
	}

	@Override
	public StepSwitchResult join(Job job, ExecutionResult exec) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StepSwitchResult stepExecError(Job job, Exception e) {
		// TODO 
		//1) Add record to step history 
		//2) Fail job 
		return null;
	}

	@Override
	public StepSwitchResult transition(Job job, Transition transition) {
		try {
			if (transition.getTransitionStatus() == TransitionStatus.NEXT_STEP) {
				job.setStatus(JobStatus.PROCESSING);
				
				long stepNumber = ((LocalWerkJob)job).getNextStepNumber();
				String stepName = transition.getStepName().get();
				Step nextStep = jobStepFactory.createNewStep(job, stepNumber, stepName);
				
				//TODO: JobManager - save step to R/O steps
				
				((WerkJob)job).setCurrentStep((WerkStep)nextStep);
			} else if (transition.getTransitionStatus() == TransitionStatus.ROLLBACK) {
				job.setStatus(JobStatus.ROLLING_BACK);
				
				long stepNumber = ((LocalWerkJob)job).getNextStepNumber();
				String stepName = transition.getStepName().get();
				
				Step nextStep = jobStepFactory.createNewStep(job, stepNumber, transition.getRollbackStepNumbers(), stepName);
				
				//TODO: JobManager - save step to R/O steps
				
				((WerkJob)job).setCurrentStep((WerkStep)nextStep);
			} else if (transition.getTransitionStatus() == TransitionStatus.FINISH) {
				job.setStatus(JobStatus.FINISHED);
				//TODO: JobManager - save step to R/O steps, save job to R/O jobs, try to put itself and child/parent jobs to eviction list
			} else if (transition.getTransitionStatus() == TransitionStatus.FAIL) {
				job.setStatus(JobStatus.FAILED);
				//TODO: JobManager - save step to R/O steps, save job to R/O jobs, try to put itself and child/parent jobs to eviction list
			}
		} catch (Exception e) {
			//Fail job
			stepTransitionError(job, e);
		}
	}

	@Override
	public StepSwitchResult stepTransitionError(Job job, Exception e) {
		// TODO 
		//1) Add record to step history 
		//2) Fail job
		
		return null;
	}
}
