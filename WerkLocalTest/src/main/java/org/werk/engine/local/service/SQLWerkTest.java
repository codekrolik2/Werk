package org.werk.engine.local.service;

import java.util.Optional;

import org.pillar.db.interfaces.TransactionFactory;
import org.pillar.db.jdbc.JDBCTransactionFactory;
import org.pillar.log4j.Log4JUtils;
import org.pillar.time.LongTimeProvider;
import org.werk.config.WerkConfig;
import org.werk.config.annotations.AnnotationsWerkConfigLoader;
import org.werk.engine.sql.SQLWerkService;
import org.werk.engine.sql.DAO.JobDAO;
import org.werk.engine.sql.DAO.JobLoadDAO;
import org.werk.engine.sql.DAO.StepDAO;
import org.werk.engine.sql.main.SQLWerkRunner;
import org.werk.rest.VertxRunner;
import org.werk.rest.WerkREST;
import org.werk.util.ParameterContextSerializer;
import org.werk.util.StepProcessingHistorySerializer;

public class SQLWerkTest {
	public static void main(String[] args) throws Exception {
		Log4JUtils.debugInitLog4j();
		
		String dbUrl = JDBCTransactionFactory.createMySQLUrl("localhost", "werk_db");
		String dbUser = "lapot";
		String dbPassword = "pillar";

		int threadCount = 4;
		int heartbeatPeriod = 15000;
		
		//Optional<Integer> jobLimit = Optional.of(500);
		//Optional<Integer> jobLimit = Optional.empty();
		Optional<Integer> jobLimit = Optional.of(5000);
		AnnotationsWerkConfigLoader<Long> loader = new AnnotationsWerkConfigLoader<>();
		WerkConfig<Long> config = loader.loadWerkConfig();
		
		LongTimeProvider timeProvider = new LongTimeProvider();
		ParameterContextSerializer parameterContextSerializer = new ParameterContextSerializer();
		StepProcessingHistorySerializer stepProcessingHistorySerializer = new StepProcessingHistorySerializer(timeProvider);
		
		StepDAO stepDAO = new StepDAO(parameterContextSerializer, stepProcessingHistorySerializer);
		JobDAO jobDAO = new JobDAO(timeProvider, parameterContextSerializer, config, stepDAO);
		JobLoadDAO jobLoadDAO = new JobLoadDAO();
		TransactionFactory transactionFactory = new JDBCTransactionFactory(dbUrl, dbUser, dbPassword);
		
		SQLWerkRunner runner = new SQLWerkRunner(transactionFactory, jobLimit, 
				threadCount, heartbeatPeriod, config, jobDAO, stepDAO, jobLoadDAO, timeProvider);

		SQLWerkService service = new SQLWerkService(config, jobDAO, stepDAO, transactionFactory, runner);
		
		/*int i = 0;
		for (i = 0; i < 1000; i++) {
			String jobTypeName = "Job1";
			Map<String, Parameter> initParameters = new HashMap<>();
			initParameters.put("text3", new StringParameterImpl("hello"));
			initParameters.put("l1", new LongParameterImpl(123L));
			initParameters.put("l2", new LongParameterImpl(1236L));
			initParameters.put("text", new StringParameterImpl("hello " + i));
			initParameters.put("d1", new DoubleParameterImpl(5.8));
			initParameters.put("text2", new StringParameterImpl("c"));
			
			JobInitInfo init = new JobInitInfoImpl(jobTypeName, Optional.of("job" + i), initParameters);
			service.createJob(init);
		}*/
	
		//SQLWerkRunner sqlWerkRunner = 
		
		VertxRunner.runVerticle(new WerkREST(service, timeProvider, false));
	}
}
