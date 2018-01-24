package org.werk.engine;

import org.pillar.exec.work.WorkThreadPool;
import org.pillar.exec.work.WorkThreadPoolRunnable;
import org.pillar.time.interfaces.TimeProvider;
import org.werk.processing.jobs.Job;

public class WerkPool extends WorkThreadPool<Job> {
	protected WerkStepSwitcher stepSwitcher;
	
	public WerkPool(int threadCount, TimeProvider timeProvider, WerkStepSwitcher stepSwitcher) {
		super(threadCount, timeProvider);
		this.stepSwitcher = stepSwitcher;
	}
	
	@Override
	protected WorkThreadPoolRunnable<Job> createRunnable() {
		return new WerkRunnable(this, stepSwitcher);
	}
}
