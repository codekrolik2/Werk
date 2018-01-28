package org.werk.engine;

import org.pillar.exec.work.WorkThreadPool;
import org.pillar.exec.work.WorkThreadPoolRunnable;
import org.pillar.time.interfaces.TimeProvider;
import org.werk.processing.jobs.Job;

public class WerkPool<J> extends WorkThreadPool<Job<J>> {
	protected WerkStepSwitcher<J> stepSwitcher;
	
	public WerkPool(int threadCount, TimeProvider timeProvider, WerkStepSwitcher<J> stepSwitcher) {
		super(threadCount, timeProvider);
		this.stepSwitcher = stepSwitcher;
	}
	
	@Override
	protected WorkThreadPoolRunnable<Job<J>> createRunnable() {
		return new WerkRunnable<J>(this, stepSwitcher);
	}
}
