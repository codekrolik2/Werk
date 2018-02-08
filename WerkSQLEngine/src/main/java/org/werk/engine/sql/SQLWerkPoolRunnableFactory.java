package org.werk.engine.sql;

import org.pillar.db.interfaces.TransactionFactory;
import org.pillar.exec.work.WorkThreadPool;
import org.pillar.exec.work.WorkThreadPoolRunnable;
import org.pillar.exec.work.WorkThreadPoolRunnableFactory;
import org.pulse.interfaces.Pulse;
import org.werk.config.WerkConfig;
import org.werk.engine.WerkCallbackRunnable;
import org.werk.engine.WerkStepSwitcher;
import org.werk.engine.sql.DAO.JobLoadDAO;
import org.werk.processing.jobs.Job;

import lombok.Setter;

public class SQLWerkPoolRunnableFactory implements WorkThreadPoolRunnableFactory<Job<Long>> {
	protected WerkStepSwitcher<Long> stepSwitcher;
	
	@Setter
	protected WorkThreadPool<Job<Long>> pool;
	@Setter
	protected WerkCallbackRunnable<Long> callbackRunnable;
	
	protected TransactionFactory connectionFactory;
	protected Pulse<Long> pulse;
	protected WerkConfig<Long> config;
	protected JobLoadDAO jobLoadDAO;
	
	public SQLWerkPoolRunnableFactory(WerkStepSwitcher<Long> stepSwitcher, TransactionFactory connectionFactory, 
			Pulse<Long> pulse, WerkConfig<Long> config, JobLoadDAO jobLoadDAO) {
		this.stepSwitcher = stepSwitcher;
		this.connectionFactory = connectionFactory;
		this.pulse = pulse;
		this.config = config;
		this.jobLoadDAO = jobLoadDAO;
	}
	
	@Override
	public WorkThreadPoolRunnable<Job<Long>> createRunnable() {
		return new SQLWerkPoolRunnable(pool, callbackRunnable, stepSwitcher, connectionFactory, 
				pulse, config, jobLoadDAO);
	}
}
