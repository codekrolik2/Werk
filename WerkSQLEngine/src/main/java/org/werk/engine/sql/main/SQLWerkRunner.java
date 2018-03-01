package org.werk.engine.sql.main;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.json.JSONObject;
import org.pillar.db.interfaces.TransactionFactory;
import org.pillar.time.LongTimeProvider;
import org.pulse.DefaultPulse;
import org.pulse.NoOpChecker;
import org.pulse.PulseRunnable;
import org.pulse.interfaces.PulseReg;
import org.pulse.interfaces.ServerChecker;
import org.pulse.interfaces.ServerPulseDAO;
import org.pulse.interfaces.ServerPulseListener;
import org.pulse.interfaces.ServerPulseRecord;
import org.pulse.jdbc.JDBCServerPulseDAO;
import org.werk.config.WerkConfig;
import org.werk.engine.sql.SQLStepSwitcher;
import org.werk.engine.sql.SQLWerkEngine;
import org.werk.engine.sql.DAO.JobDAO;
import org.werk.engine.sql.DAO.JobLoadDAO;
import org.werk.engine.sql.DAO.StepDAO;
import org.werk.engine.sql.pulse.WerkPulseRecordCleaner;

import lombok.Getter;

public class SQLWerkRunner {
	protected PulseRunnable<Long> pulseRunnable;
	@Getter
	protected PulseReg<Long> pulse;
	
	protected Optional<Integer> jobLimit;
	@Getter
	protected AtomicReference<SQLWerkEngine> currentEngine;
	
	public SQLWerkRunner(TransactionFactory connectionFactory, 
			Optional<Integer> jobLimit, int threadCount, int heartbeatPeriod,
			WerkConfig<Long> config, JobDAO jobDAO, StepDAO stepDAO, JobLoadDAO jobLoadDAO,
			LongTimeProvider timeProvider) {
		this.jobLimit = jobLimit;
		currentEngine = new AtomicReference<>(null);
		
		//----------------PULSE----------------
		
		ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
		
		ServerPulseDAO<Long> serverDAO = new JDBCServerPulseDAO(timeProvider);
		
		ServerChecker<Long> serverChecker = new NoOpChecker<>();
		pulse = new DefaultPulse<>(connectionFactory, serverDAO, serverChecker, heartbeatPeriod);
		pulse.clearServerRecordCleaners();
		pulse.addServerRecordCleaner(new WerkPulseRecordCleaner(jobLoadDAO, serverDAO));
		
		Supplier<String> serverInfoGetter = new Supplier<String>() {
			@Override
			public String get() {
				JSONObject serverInfo = new JSONObject();
				
				SQLWerkEngine engine = currentEngine.get();
				serverInfo.put("jobCount", engine == null ? 0 : engine.getJobCount());
				
				if (jobLimit.isPresent())
					serverInfo.put("jobLimit", jobLimit.get());
				
				return serverInfo.toString();
			}
		};
		
		pulseRunnable = new PulseRunnable<Long>(scheduler, pulse, serverInfoGetter, timeProvider, heartbeatPeriod, 1000);
		scheduler.execute(pulseRunnable);
		
		//-----------SQL WERK ENGINE-----------
		
		SQLStepSwitcher stepSwitcher = new SQLStepSwitcher(timeProvider, jobDAO, stepDAO, config);
		
		pulse.addServerPulseListener(new ServerPulseListener<Long>() {
			@Override
			public void hbCreated(ServerPulseRecord<Long> server) {
				SQLWerkEngine engine = new SQLWerkEngine(threadCount, stepSwitcher, connectionFactory, pulse, config,
						jobDAO, stepDAO, serverDAO, jobLoadDAO, heartbeatPeriod);
				if (currentEngine.compareAndSet(null, engine))
					engine.start();
			}
			
			@Override
			public void hbLost(Exception e) {
				SQLWerkEngine engine = currentEngine.get();
				if (currentEngine.compareAndSet(engine, null))
					engine.shutdown();
			}
			
			@Override public void hbUpdated(ServerPulseRecord<Long> server) { }
		});
		
		pulse.addServerRecordCleaner(new WerkPulseRecordCleaner(jobLoadDAO, serverDAO));
	}
}
