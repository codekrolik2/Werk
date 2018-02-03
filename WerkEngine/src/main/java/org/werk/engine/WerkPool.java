package org.werk.engine;

import org.pillar.exec.work.WorkThreadPool;
import org.pillar.exec.work.WorkThreadPoolRunnable;
import org.pillar.time.interfaces.TimeProvider;
import org.werk.processing.jobs.Job;

public class WerkPool<J> extends WorkThreadPool<Job<J>> {
	protected WerkStepSwitcher<J> stepSwitcher;
	protected WerkCallbackRunnable<J> callbackRunnable;
	
	public WerkPool(int threadCount, TimeProvider timeProvider, WerkCallbackRunnable<J> callbackRunnable,
			WerkStepSwitcher<J> stepSwitcher) {
		super(timeProvider);
		this.callbackRunnable = callbackRunnable;
		this.stepSwitcher = stepSwitcher;
		adjustSize(threadCount);
	}
	
	@Override
	protected WorkThreadPoolRunnable<Job<J>> createRunnable() {
		return new WerkPoolRunnable<J>(this, callbackRunnable, stepSwitcher);
	}
}
