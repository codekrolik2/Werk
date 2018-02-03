package org.werk.engine;

import org.pillar.time.LongTimeProvider;
import org.pillar.time.interfaces.TimeProvider;
import org.werk.engine.processing.WerkJob;
import org.werk.processing.jobs.Job;

public class WerkEngineImpl<J> implements WerkEngine<J> {
	protected TimeProvider timeProvider;
	protected WerkPool<J> werkPool;
	protected WerkCallbackRunnable<J> callbackRunnable;
	
	public WerkEngineImpl(int threadCount, WerkStepSwitcher<J> stepSwitcher) {
		timeProvider = new LongTimeProvider();
		
		callbackRunnable = new WerkCallbackRunnable<J>(timeProvider);
		werkPool = new WerkPool<J>(threadCount, timeProvider, callbackRunnable, stepSwitcher);
		callbackRunnable.setPool(werkPool);
		
		new Thread(callbackRunnable, "WerkEngine Callback Runnable").start();
	}

	@Override
	public void addJob(Job<J> job) {
		long delayMs = timeProvider.getCurrentTime().getDeltaInMs(job.getNextExecutionTime());
		werkPool.addUnitOfWork((WerkJob<J>)job, delayMs);
	}

	@Override
	public void shutdown() {
		werkPool.shutdown();
		callbackRunnable.shutdown();
	}
}
