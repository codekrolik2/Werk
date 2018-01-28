package org.werk.engine;

import org.pillar.time.LongTimeProvider;
import org.pillar.time.interfaces.TimeProvider;
import org.werk.engine.processing.WerkJob;
import org.werk.processing.jobs.Job;

public class WerkEngineImpl<J> implements WerkEngine<J> {
	protected WerkPool<J> werkPool;
	protected WerkStepSwitcher<J> stepSwitcher;
	protected TimeProvider timeProvider;
	
	public WerkEngineImpl(int threadCount, WerkStepSwitcher<J> stepSwitcher) {
		timeProvider = new LongTimeProvider();
		werkPool = new WerkPool<J>(threadCount, timeProvider, stepSwitcher);
	}

	@Override
	public void addJob(Job<J> job) {
		long delayMs = timeProvider.getCurrentTime().getDeltaInMs(job.getNextExecutionTime());
		werkPool.addUnitOfWork((WerkJob<J>)job, delayMs);
	}
}
