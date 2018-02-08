package org.werk.engine;

import org.pillar.exec.work.WorkThreadPool;
import org.pillar.exec.work.WorkThreadPoolRunnable;
import org.pillar.exec.work.WorkThreadPoolRunnableFactory;
import org.werk.processing.jobs.Job;

import lombok.Setter;

public class WerkPoolRunnableFactory<J> implements WorkThreadPoolRunnableFactory<Job<J>> {
	protected WerkStepSwitcher<J> stepSwitcher;
	
	@Setter
	protected WorkThreadPool<Job<J>> pool;
	@Setter
	protected WerkCallbackRunnable<J> callbackRunnable;
	
	public WerkPoolRunnableFactory(WerkStepSwitcher<J> stepSwitcher) {
		this.stepSwitcher = stepSwitcher;
	}
	
	@Override
	public WorkThreadPoolRunnable<Job<J>> createRunnable() {
		return new WerkPoolRunnable<J>(pool, callbackRunnable, stepSwitcher);
	}
}
