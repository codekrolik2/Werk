package org.werk.engine.sql.main;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

import org.pillar.db.interfaces.TransactionFactory;
import org.pillar.db.jdbc.JDBCTransactionFactory;
import org.pillar.time.LongTimeProvider;
import org.pulse.DefaultPulse;
import org.pulse.NoOpChecker;
import org.pulse.PulseRunnable;
import org.pulse.interfaces.PulseReg;
import org.pulse.interfaces.ServerChecker;
import org.pulse.interfaces.ServerPulseDAO;
import org.pulse.jdbc.JDBCServerPulseDAO;

public class SQLWerkRunner {
	protected PulseRunnable<Long> pulseRunnable;
	protected PulseReg<Long> pulse;
	
	public SQLWerkRunner(String dbUrl, String dbUser, String dbPassword, int heartbeatPeriod) {
		//-----------PULSE-----------
		LongTimeProvider timeProvider = new LongTimeProvider();
		ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
		
		TransactionFactory connectionFactory = new JDBCTransactionFactory(dbUrl, dbUser, dbPassword);
		ServerPulseDAO<Long> serverDAO = new JDBCServerPulseDAO(timeProvider);
		
		ServerChecker<Long> serverChecker = new NoOpChecker<>();
		pulse = new DefaultPulse<>(connectionFactory, serverDAO, serverChecker, heartbeatPeriod);
		Supplier<String> serverInfoGetter = new Supplier<String>() {
			@Override
			public String get() {
				//TODO: { "jobCount" : 123, "jobLimit" : 456 }
				return "WerkServer";
			}
		};
		
		pulseRunnable = new PulseRunnable<Long>(scheduler, pulse, serverInfoGetter, timeProvider, heartbeatPeriod);
		scheduler.execute(pulseRunnable);
		
		//-----------PULSE-----------

	}
}
