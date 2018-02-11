package org.werk.engine.sql;

import org.pillar.db.interfaces.TransactionFactory;
import org.pillar.exec.work.WorkThreadPool;
import org.pillar.exec.workdistribution.FairWorkDistributionCalc;
import org.pillar.time.LongTimeProvider;
import org.pillar.time.interfaces.TimeProvider;
import org.pulse.interfaces.Pulse;
import org.pulse.interfaces.ServerPulseDAO;
import org.werk.config.WerkConfig;
import org.werk.engine.JobIdSerializer;
import org.werk.engine.WerkCallbackRunnable;
import org.werk.engine.WerkEngine;
import org.werk.engine.WerkStepSwitcher;
import org.werk.engine.processing.WerkJob;
import org.werk.engine.sql.DAO.JobDAO;
import org.werk.engine.sql.DAO.JobLoadDAO;
import org.werk.engine.sql.DAO.StepDAO;
import org.werk.engine.sql.jobload.SQLJobLoader;
import org.werk.engine.sql.jobload.SQLJobLoaderRunnable;
import org.werk.processing.jobs.Job;
import org.werk.util.JoinResultSerializer;
import org.werk.util.LongJobIdSerializer;

public class SQLWerkEngine implements WerkEngine<Long> {
	protected final TimeProvider timeProvider;
	protected final WorkThreadPool<Job<Long>> werkPool;
	protected final SQLWerkPoolRunnableFactory runnableFactory;
	protected final WerkCallbackRunnable<Long> callbackRunnable;
	protected final SQLJobLoaderRunnable sqlJobLoaderRunnable;
	protected final int threadCount;
	
	public SQLWerkEngine(int threadCount, WerkStepSwitcher<Long> stepSwitcher, 
			TransactionFactory connectionFactory, Pulse<Long> pulse, WerkConfig<Long> config, 
			JobDAO jobDAO, StepDAO stepDAO, ServerPulseDAO<Long> serverDAO, JobLoadDAO jobLoadDAO,
			long jobLoadPeriodMS) {
		this.threadCount = threadCount;
		timeProvider = new LongTimeProvider();
		
		callbackRunnable = new WerkCallbackRunnable<Long>(timeProvider);
		
		runnableFactory = new SQLWerkPoolRunnableFactory(stepSwitcher, connectionFactory, 
				pulse, config, jobLoadDAO);
		
		werkPool = new WorkThreadPool<Job<Long>>(timeProvider, runnableFactory);
		
		callbackRunnable.setPool(werkPool);
		
		runnableFactory.setPool(werkPool);
		runnableFactory.setCallbackRunnable(callbackRunnable);
		
		JobIdSerializer<Long> jobIdSerializer = new LongJobIdSerializer();
		JoinResultSerializer<Long> joinResultSerializer = new JoinResultSerializer<Long>(jobIdSerializer);
		FairWorkDistributionCalc workCalc = new FairWorkDistributionCalc();
		SQLJobLoader sqlJobLoader = new SQLJobLoader(jobLoadDAO, jobDAO, stepDAO, serverDAO,
				joinResultSerializer, workCalc, connectionFactory, pulse,
				this, config, timeProvider);
		sqlJobLoaderRunnable = new SQLJobLoaderRunnable(sqlJobLoader, jobLoadPeriodMS);
	}

	public void start() {
		new Thread(callbackRunnable, "WerkEngine Callback Runnable").start();
		new Thread(sqlJobLoaderRunnable, "WerkEngine JobLoad Runnable").start();
		werkPool.start(threadCount);
	}
	
	@Override
	public void addJob(Job<Long> job) {
		long delayMs = timeProvider.getCurrentTime().getDeltaInMs(job.getNextExecutionTime());
		werkPool.addUnitOfWork((WerkJob<Long>)job, delayMs);
	}

	@Override
	public void shutdown() {
		callbackRunnable.shutdown();
		sqlJobLoaderRunnable.shutdown();
		werkPool.shutdown();
	}
	
	public int getJobCount() {
		int qJobs = werkPool.getJobsInQ();
		int inFlightJobs = werkPool.getJobsInFlight();
		int jobsWaitingForCallback = callbackRunnable.getCallbacksCount();
		
		return qJobs + inFlightJobs + jobsWaitingForCallback;
	}
}
