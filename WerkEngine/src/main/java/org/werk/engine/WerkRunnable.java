package org.werk.engine;

import org.pillar.exec.work.WorkThreadPool;
import org.pillar.exec.work.WorkThreadPoolRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.werk.processing.jobs.Job;
import org.werk.processing.jobs.JobStatus;
import org.werk.processing.steps.StepExec;
import org.werk.processing.steps.StepExecutionResult;
import org.werk.processing.steps.StepTransitioner;
import org.werk.processing.steps.Transition;

public class WerkRunnable extends WorkThreadPoolRunnable<Job> {
	final Logger logger = LoggerFactory.getLogger(WerkRunnable.class);
	
	protected WerkStepSwitcher stepSwitcher;
	
	public WerkRunnable(WorkThreadPool<Job> pool, WerkStepSwitcher stepSwitcher) {
		super(pool);
		this.stepSwitcher = stepSwitcher;
	}
	
	@Override
	public void process(Job job) {
		Exception execException = null;
		StepExecutionResult result = null;
		try {
			//get stepExec
			StepExec stepExec = job.getCurrentStep().getStepExec();
			
			//open Job/Step context and inject properties
			job.openTempContextAndRemap(stepExec);
			
			//execute stepExec
			if (job.getStatus() == JobStatus.ROLLING_BACK)
				result = stepExec.rollback(job.getCurrentStep());
			else
				result = stepExec.process(job.getCurrentStep());
			
			//commit Job/Step context
			job.commitTempContext();
		} catch (Exception e) {
			execException = e;
		}
		
		Exception transitionException = null;
		Transition transition = null;
		if (execException == null) {
			try {
				//get stepTransitioner
				StepTransitioner transitioner = job.getCurrentStep().getStepTransitioner();
				
				//open Job/Step context and inject properties
				job.openTempContextAndRemap(transitioner);
				
				//execute stepTransitioner
				transition = transitioner.transition(result, job.getCurrentStep());
				
				//commit Job/Step context
				job.commitTempContext();
			} catch (Exception e) {
				transitionException = e;
			}
		}
		
		//switch step
		try {
			StepSwitchResult switchResult;
			if (execException != null) {
				switchResult = stepSwitcher.stepExecError(job, execException);
			} else if (transitionException != null) {
				switchResult = stepSwitcher.stepTransitionError(job, transitionException);
			} else {
				switchResult = stepSwitcher.switchStep(job, transition);
			}
			
			if (switchResult == StepSwitchResult.PROCESS) {
				long delayMS = 0;
				if (result != null)
					if (result.getDelayMS().isPresent())
						delayMS = result.getDelayMS().get();
						
				pool.addUnitOfWork(job, delayMS);
			} else
				logger.info("Processing finished: job [%s]", job.toString());
		} catch(Exception e) {
			logger.error(String.format("Switcher error: job [%s]", job.toString()), e);
		}
	}
}
