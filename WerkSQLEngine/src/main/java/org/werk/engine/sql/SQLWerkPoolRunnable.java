package org.werk.engine.sql;

import org.apache.log4j.Logger;
import org.pillar.db.interfaces.TransactionContext;
import org.pillar.db.interfaces.TransactionFactory;
import org.pillar.exec.work.WorkThreadPool;
import org.pulse.interfaces.Pulse;
import org.werk.config.WerkConfig;
import org.werk.engine.WerkCallbackRunnable;
import org.werk.engine.WerkPoolRunnable;
import org.werk.engine.WerkStepSwitcher;
import org.werk.engine.sql.DAO.JobLoadDAO;
import org.werk.engine.sql.exception.SQLWerkPoolRunnableException;
import org.werk.processing.jobs.Job;

public class SQLWerkPoolRunnable extends WerkPoolRunnable<Long> {
	final Logger logger = Logger.getLogger(SQLWerkPoolRunnable.class);
	
	protected TransactionFactory connectionFactory;
	protected Pulse<Long> pulse;
	protected WerkConfig<Long> config;
	protected JobLoadDAO jobLoadDAO;
	
	public SQLWerkPoolRunnable(WorkThreadPool<Job<Long>> pool, WerkCallbackRunnable<Long> callbackRunnable,
			WerkStepSwitcher<Long> stepSwitcher, TransactionFactory connectionFactory, Pulse<Long> pulse,
			WerkConfig<Long> config, JobLoadDAO jobLoadDAO) {
		super(pool, callbackRunnable, stepSwitcher);
		this.connectionFactory = connectionFactory;
		this.pulse = pulse;
		this.config = config;
		this.jobLoadDAO = jobLoadDAO;
	}
	
	@Override
	public void process(Job<Long> job) {
		try {
			super.process(job);
		} catch (Exception e) {
			logger.info("SQLWerkPoolRunnable exception: losing heartbeat", e);
			pulse.loseHeartbeat(e);
		}
	}
	
	@Override
	protected void beforeExec(Job<Long> job) {
		try {
			//Create and inject transaction if not ShortTransaction
			if (!job.getCurrentStep().getStepType().isShortTransaction()) {
				TransactionContext stepTransactionContext = connectionFactory.startTransaction();
				((SQLWerkJob)job).setStepTransactionContext(stepTransactionContext);
			}
		} catch (Exception e) {
			throw new SQLWerkPoolRunnableException(e);
		}
	}
	
	@Override
	protected void beforeSwitch(Job<Long> job) {
		try {
			//Create and inject transaction if ShortTransaction
			if (job.getCurrentStep().getStepType().isShortTransaction()) {
				TransactionContext stepTransactionContext = connectionFactory.startTransaction();
				((SQLWerkJob)job).setStepTransactionContext(stepTransactionContext);
			}
		} catch (Exception e) {
			throw new SQLWerkPoolRunnableException(e);
		}
	}
	
	@Override
	protected void afterSwitch(Job<Long> job) {
		//Update all forked jobs status, UNDEFINED -> PROCESSING
		//Commit transaction
		try {
			SQLWerkJob sqlWerkJob = (SQLWerkJob)job;
			
			jobLoadDAO.activateForkedChildJobs(sqlWerkJob.getStepTransactionContext(), sqlWerkJob.getForkedJobs());
			
			sqlWerkJob.getForkedJobs().clear();
			
			sqlWerkJob.getStepTransactionContext().commit();
		} catch (Exception e) {
			throw new SQLWerkPoolRunnableException(e);
		}
	}
}
