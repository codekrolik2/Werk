package org.werk.engine;

import org.apache.log4j.Logger;
import org.pillar.exec.work.WorkThreadPool;
import org.pillar.exec.work.WorkThreadPoolRunnable;
import org.werk.processing.jobs.Job;
import org.werk.processing.jobs.JobStatus;
import org.werk.processing.steps.ExecutionResult;
import org.werk.processing.steps.StepExec;
import org.werk.processing.steps.StepExecutionStatus;
import org.werk.processing.steps.Transition;
import org.werk.processing.steps.Transitioner;

public class WerkPoolRunnable<J> extends WorkThreadPoolRunnable<Job<J>> {
	final Logger logger = Logger.getLogger(WerkPoolRunnable.class);
	
	protected WerkStepSwitcher<J> stepSwitcher;
	protected WerkCallbackRunnable<J> callbackRunnable;
	
	public WerkPoolRunnable(WorkThreadPool<Job<J>> pool, WerkCallbackRunnable<J> callbackRunnable, 
			WerkStepSwitcher<J> stepSwitcher) {
		super(pool);
		this.callbackRunnable = callbackRunnable;
		this.stepSwitcher = stepSwitcher;
	}
	
	@Override
	public void process(Job<J> job) {
		Exception execException = null;
		ExecutionResult<J> execResult = null;
		
		beforeExec(job);
		
		try {
			job.getCurrentStep().incrementExecutionCount();
			
			//get stepExec
			StepExec<J> stepExec = job.getCurrentStep().getStepExec();
			
			//open Job/Step context and inject properties
			job.openTempContextAndRemap(stepExec);
			
			//execute stepExec
			if (job.getStatus() == JobStatus.ROLLING_BACK)
				execResult = stepExec.rollback(job.getCurrentStep());
			else
				execResult = stepExec.process(job.getCurrentStep());
			
			//commit Job/Step context
			job.commitTempContext();
		} catch (Exception e) {
			job.rollbackTempContext();
			execException = e;
		}
		
		beforeTransition(job);
		
		Exception transitionException = null;
		Transition transition = null;
		if ((execResult.getStatus() != StepExecutionStatus.REDO) &&
			(execResult.getStatus() != StepExecutionStatus.JOIN)) {
			if (execException == null) {
				try {
					//get stepTransitioner
					Transitioner<J> transitioner = job.getCurrentStep().getStepTransitioner();
					
					//open Job/Step context and inject properties
					job.openTempContextAndRemap(transitioner);
					
					if (job.getStatus() == JobStatus.PROCESSING)
						transition = transitioner.processingTransition(
								execResult.getStatus() == StepExecutionStatus.SUCCESS, job.getCurrentStep());
					else
						transition = transitioner.rollbackTransition(
								execResult.getStatus() == StepExecutionStatus.SUCCESS, job.getCurrentStep());
					
					//commit Job/Step context
					job.commitTempContext();
				} catch (Exception e) {
					job.rollbackTempContext();
					transitionException = e;
				}
			}
		}
		
		//switch step
		beforeSwitch(job);
		
		StepSwitchResult switchResult;
		
		if (execException != null)
			switchResult = stepSwitcher.stepExecError(job, execException);
		else if (transitionException != null)
			switchResult = stepSwitcher.stepTransitionError(job, transitionException);
		else if (execResult.getStatus() == StepExecutionStatus.REDO)
			switchResult = stepSwitcher.redo(job, execResult);
		else if (execResult.getStatus() == StepExecutionStatus.JOIN)
			switchResult = stepSwitcher.join(job, execResult);
		else if (execResult.getStatus() == StepExecutionStatus.CALLBACK)
			switchResult = stepSwitcher.callback(job, execResult);
		else
			switchResult = stepSwitcher.transition(job, transition);
		
		if (switchResult.getStatus() == SwitchStatus.PROCESS) {
			long delayMS = 0;
			if (switchResult.getDelayMS().isPresent())
				delayMS = execResult.getDelayMS().get();
			
			pool.addUnitOfWork(job, delayMS);
		} else if (switchResult.getStatus() == SwitchStatus.CALLBACK) {
			callbackRunnable.addCallback(switchResult.getCallback().get(), job, switchResult.getDelayMS(), 
					switchResult.getParameterName().get());
		} else if (switchResult.getStatus() == SwitchStatus.UNLOAD) {
			logger.info(
					String.format("Unloading job: [%s / %s / %s]",
							job.getJobTypeName(),
							job.getJobName().isPresent() ? job.getJobName().get() : "No name",
							job.getStatus())
				);
		} else 
			logger.error(
					String.format("Error - Unknown SwitchStatus: [%s]. Unloading job: [%s / %s / %s]",
							switchResult.getStatus(),
							job.getJobTypeName(),
							job.getJobName().isPresent() ? job.getJobName().get() : "No name",
							job.getStatus())
				);
		
		afterSwitch(job);
	}

	protected void beforeExec(Job<J> job) {}
	protected void beforeTransition(Job<J> job) {}
	protected void beforeSwitch(Job<J> job) {}
	protected void afterSwitch(Job<J> job) {}
}
