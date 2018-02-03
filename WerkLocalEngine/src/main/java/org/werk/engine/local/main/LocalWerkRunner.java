package org.werk.engine.local.main;

import java.util.concurrent.atomic.AtomicLong;

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

import lombok.Getter;

public class LocalWerkRunner {
	@Getter
	protected final LocalJobManager<Long> localJobManager;
	@Getter
	protected final LocalJobStepFactory<Long> jobStepFactory;
	@Getter
	protected final LocalStepSwitcher<Long> switcher;
	
	@Getter
	protected final WerkEngine<Long> werkEngine;
	@Getter
	protected final WerkConfig<Long> werkConfig;
	@Getter
	protected final LocalWerkService service;

	@Getter
	protected final int maxJobCacheSize;
	@Getter
	protected final int threadCount;
	
	public static LocalWerkRunner createAnnotationsConfig(int maxJobCacheSize, int threadCount) throws WerkConfigException {
		AnnotationsWerkConfigLoader<Long> loader = new AnnotationsWerkConfigLoader<>();
		return new LocalWerkRunner(loader.loadWerkConfig(), maxJobCacheSize, threadCount);
	}
	
	public static LocalWerkRunner createCustomConfig(WerkConfig<Long> werkConfig, int maxJobCacheSize, int threadCount) throws WerkConfigException {
		return new LocalWerkRunner(werkConfig, maxJobCacheSize, threadCount);
	}
	
	protected LocalWerkRunner(WerkConfig<Long> werkConfig, int maxJobCacheSize, int threadCount) throws WerkConfigException {
		this.werkConfig = werkConfig;
		this.maxJobCacheSize = maxJobCacheSize;
		this.threadCount = threadCount;
		
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
}
