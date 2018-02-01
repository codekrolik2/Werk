package org.werk.engine.local.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.pillar.log4j.Log4JUtils;
import org.pillar.time.LongTimeProvider;
import org.werk.config.WerkConfig;
import org.werk.config.annotations.AnnotationsWerkConfigLoader;
import org.werk.engine.WerkEngine;
import org.werk.engine.WerkEngineImpl;
import org.werk.engine.json.JoinResultSerializer;
import org.werk.engine.json.LongJobIdSerializer;
import org.werk.engine.local.LocalJobManager;
import org.werk.engine.local.LocalJobStepFactory;
import org.werk.engine.local.LocalStepSwitcher;
import org.werk.engine.local.LocalWerkService;
import org.werk.exceptions.WerkConfigException;
import org.werk.meta.JobInitInfo;
import org.werk.meta.JobType;
import org.werk.meta.impl.JobInitInfoImpl;
import org.werk.processing.parameters.Parameter;
import org.werk.processing.parameters.impl.DoubleParameterImpl;
import org.werk.processing.parameters.impl.LongParameterImpl;
import org.werk.processing.parameters.impl.StringParameterImpl;

import lombok.Getter;

public class LocalWerkRunner {
	protected LocalJobManager<Long> localJobManager;
	protected LocalJobStepFactory<Long> jobStepFactory;
	protected LocalStepSwitcher<Long> switcher;
	
	protected WerkEngine<Long> werkEngine;
	protected WerkConfig<Long> werkConfig;
	protected long maxJobCacheSize;
	@Getter
	protected LocalWerkService service;
	
	public LocalWerkRunner(int maxJobCacheSize, int threadCount) throws WerkConfigException {
		AnnotationsWerkConfigLoader<Long> loader = new AnnotationsWerkConfigLoader<>();
		werkConfig = loader.loadWerkConfig();
		
		localJobManager = new LocalJobManager<Long>(maxJobCacheSize);
		
		jobStepFactory = new LocalJobStepFactory<Long>(werkConfig, new LongTimeProvider(), localJobManager,
				new JoinResultSerializer<Long>(new LongJobIdSerializer())) {
			AtomicLong jobIdCounter = new AtomicLong(0L);
			@Override
			protected Long getNextJobId() {
				return jobIdCounter.incrementAndGet();
			}
		};
		localJobManager.setJobStepFactory(jobStepFactory);
		
		switcher = new LocalStepSwitcher<Long>(jobStepFactory, localJobManager);
		
		werkEngine = new WerkEngineImpl<Long>(threadCount, switcher);
		localJobManager.setWerkEngine(werkEngine);
		
		service = new LocalWerkService(localJobManager, werkConfig);
	}
	
	public static void main(String[] args) throws Exception {
		Log4JUtils.debugInitLog4j();
		
		int maxJobCacheSize = 50000;
		int threadCount = 4;
		
		LocalWerkRunner localWerkRunner = new LocalWerkRunner(maxJobCacheSize, threadCount);
		LocalWerkService service = localWerkRunner.getService();
		
		for (JobType type : localWerkRunner.werkConfig.getAllJobTypes())
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
			
			JobInitInfo init = new JobInitInfoImpl(jobTypeName, "job" + i, initParameters);
			service.createJob(init);
		//}
	}
}
