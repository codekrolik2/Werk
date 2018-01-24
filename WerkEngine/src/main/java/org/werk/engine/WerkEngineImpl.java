package org.werk.engine;

import org.commandline.CommandLineProcessor;
import org.pillar.time.LongTimeProvider;
import org.pillar.time.interfaces.TimeProvider;
import org.werk.engine.processing.WerkJob;
import org.werk.processing.jobs.Job;

public class WerkEngineImpl extends CommandLineProcessor implements WerkEngine {
	protected WerkPool werkPool;
	protected WerkStepSwitcher stepSwitcher;
	protected TimeProvider timeProvider;
	
	public WerkEngineImpl(int threadCount, WerkStepSwitcher stepSwitcher) {
		timeProvider = new LongTimeProvider();
		werkPool = new WerkPool(threadCount, timeProvider, stepSwitcher);
	}

	@Override
	public void addJob(Job job) {
		long delayMs = timeProvider.getCurrentTime().getDeltaInMs(job.getNextExecutionTime());
		werkPool.addUnitOfWork((WerkJob)job, delayMs);
	}

	//----------------------------------------------------
	
	public static void main(String[] args) throws Exception {
		WerkEngineImpl engine = new WerkEngineImpl(16, null);
		engine.readInput();
	}

	@Override
	protected void processCommand(String command) throws Exception {
		System.out.println(command);
	}
}
