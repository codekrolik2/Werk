package org.werk.engine;

import org.pillar.exec.work.WorkThreadPool;
import org.pillar.time.LongTimeProvider;
import org.pillar.time.interfaces.TimeProvider;
import org.werk.engine.processing.WerkJob;
import org.werk.processing.jobs.Job;

public class WerkEngineImpl<J> implements WerkEngine<J> {
	protected TimeProvider timeProvider;
	protected WorkThreadPool<Job<J>> werkPool;
	protected WerkPoolRunnableFactory<J> runnableFactory;
	protected WerkCallbackRunnable<J> callbackRunnable;
	
	public WerkEngineImpl(int threadCount, WerkStepSwitcher<J> stepSwitcher) {
		timeProvider = new LongTimeProvider();
		
		callbackRunnable = new WerkCallbackRunnable<J>(timeProvider);
		
		runnableFactory = new WerkPoolRunnableFactory<J>(stepSwitcher);
		werkPool = new WorkThreadPool<Job<J>>(timeProvider, runnableFactory);
		
		callbackRunnable.setPool(werkPool);
		new Thread(callbackRunnable, "WerkEngine Callback Runnable").start();
		
		runnableFactory.setPool(werkPool);
		runnableFactory.setCallbackRunnable(callbackRunnable);
		
		werkPool.start(threadCount);
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
