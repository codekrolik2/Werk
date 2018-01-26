package org.werk.engine.local;

import java.util.List;

import org.pillar.time.interfaces.TimeProvider;
import org.werk.config.WerkConfig;
import org.werk.engine.JobStepFactory;
import org.werk.meta.JobInitInfo;
import org.werk.meta.OldVersionJobInitInfo;
import org.werk.processing.readonly.ReadOnlyJob;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class LocalJobManager {
	@AllArgsConstructor
	class JobCluster {
		@Getter
		protected ReadOnlyJob parentJob;
		@Getter
		protected List<ReadOnlyJob> childJobs;
	}
	
	protected WerkConfig werkConfig;
	protected TimeProvider timeProvider;
	protected JobStepFactory jobStepFactory;
	
	public void createJob(JobInitInfo init) throws Exception {
		jobStepFactory.createNewJob(init.getJobTypeName(), init.getInitParameters(), init.getJobName());
	}
	
	public void createOldVersionJob(OldVersionJobInitInfo init) throws Exception {
		jobStepFactory.createOldVersionJob(init.getJobTypeName(), init.getOldVersion(), 
				init.getInitParameters(), init.getJobName());
	}
	
	//---------------------------------------------------
	
	
}
