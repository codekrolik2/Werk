package org.werk.engine.local.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.pillar.log4j.Log4JUtils;
import org.werk.engine.local.LocalWerkService;
import org.werk.engine.local.main.LocalWerkRunner;
import org.werk.meta.JobInitInfo;
import org.werk.meta.JobType;
import org.werk.meta.impl.JobInitInfoImpl;
import org.werk.processing.parameters.Parameter;
import org.werk.processing.parameters.impl.DoubleParameterImpl;
import org.werk.processing.parameters.impl.LongParameterImpl;
import org.werk.processing.parameters.impl.StringParameterImpl;

public class LocalWerkTest {
	public static void main(String[] args) throws Exception {
		Log4JUtils.debugInitLog4j();
		
		int maxJobCacheSize = 50000;
		int threadCount = 4;
		
		LocalWerkRunner localWerkRunner = LocalWerkRunner.createAnnotationsConfig(maxJobCacheSize, threadCount);
		LocalWerkService service = localWerkRunner.getService();
		
		for (JobType type : localWerkRunner.getWerkConfig().getAllJobTypes())
			System.out.println(type.toString());
		
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
			
			JobInitInfo init = new JobInitInfoImpl(jobTypeName, Optional.of("job" + i), initParameters);
			service.createJob(init);
		//}
	}
}
