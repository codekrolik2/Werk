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
import org.werk.engine.local.LocalJobManager;
import org.werk.engine.local.LocalJobStepFactory;
import org.werk.engine.local.LocalStepSwitcher;
import org.werk.exceptions.WerkConfigException;
import org.werk.meta.JobInitInfo;
import org.werk.meta.JobType;
import org.werk.processing.parameters.Parameter;
import org.werk.processing.parameters.impl.LongParameterImpl;
import org.werk.processing.parameters.impl.StringParameterImpl;

import lombok.Getter;

public class LocalWerkRunner {
	protected LocalJobManager<Long> localJobManager;
	protected LocalJobStepFactory<Long> jobStepFactory;
	protected LocalStepSwitcher<Long> switcher;
	
	protected WerkEngine<Long> werkEngine;
	protected long maxJobCacheSize;
	@Getter
	protected LocalWerkService service;
	
	public LocalWerkRunner(int maxJobCacheSize, int threadCount) throws WerkConfigException {
		AnnotationsWerkConfigLoader<Long> loader = new AnnotationsWerkConfigLoader<>();
		WerkConfig<Long> werkConfig = loader.loadWerkConfig();
		
		localJobManager = new LocalJobManager<Long>(maxJobCacheSize);
		
		jobStepFactory = new LocalJobStepFactory<Long>(werkConfig, new LongTimeProvider(), localJobManager) {
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
		
		for (JobType type : service.werkConfig.getAllJobTypes())
			System.out.println(type.toString());
		
		String jobTypeName = "Job1";
		Map<String, Parameter> initParameters = new HashMap<>();
		initParameters.put("text3", new StringParameterImpl("hello"));
		initParameters.put("l2", new LongParameterImpl(123L));
		
		JobInitInfo init = new JobInitInfoImpl(jobTypeName, initParameters);
		service.createJob(init);
	}
}
