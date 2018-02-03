package org.werk.engine.local.service;

import java.util.HashMap;
import java.util.Map;

import org.pillar.db.jdbc.JDBCTransactionFactory;
import org.pillar.log4j.Log4JUtils;
import org.werk.engine.sql.main.SQLWerkRunner;
import org.werk.meta.JobInitInfo;
import org.werk.meta.impl.JobInitInfoImpl;
import org.werk.processing.parameters.Parameter;
import org.werk.processing.parameters.impl.DoubleParameterImpl;
import org.werk.processing.parameters.impl.LongParameterImpl;
import org.werk.processing.parameters.impl.StringParameterImpl;

public class SQLWerkTest {
	public static void main(String[] args) throws Exception {
		Log4JUtils.debugInitLog4j();
		
		int threadCount = 4;
		
		String dbUrl = JDBCTransactionFactory.createMySQLUrl("localhost", "werk_db");
		String dbUser = "lapot";
		String dbPassword = "pillar";
		int heartbeatPeriod = 15000;
		
		SQLWerkRunner sqlWerkRunner = new SQLWerkRunner(dbUrl, dbUser, dbPassword, heartbeatPeriod);
		//sqlWerkRunner = SQLWerkRunnner.createAnnotationsConfig(maxJobCacheSize, threadCount);
		//LocalWerkService service = sqlWerkRunner.getService();
		
		/*for (JobType type : sqlWerkRunner.getWerkConfig().getAllJobTypes())
			System.out.println(type.toString());
		*/
		int i = 0;
		//for (int i = 0; i < 1000; i++) {
			String jobTypeName = "Job1";
			Map<String, Parameter> initParameters = new HashMap<>();
			initParameters.put("text3", new StringParameterImpl("hello"));
			initParameters.put("l1", new LongParameterImpl(123L));
			initParameters.put("l2", new LongParameterImpl(1236L));
			initParameters.put("text", new StringParameterImpl("hello " + i));
			initParameters.put("d1", new DoubleParameterImpl(5.8));
			initParameters.put("text2", new StringParameterImpl("c"));
			
			JobInitInfo init = new JobInitInfoImpl(jobTypeName, "job" + i, initParameters);
			//service.createJob(init);
		//}
	}
}
