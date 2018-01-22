package org.werk.config.interfaces;

import java.util.Map;

import org.werk.meta.JobType;
import org.werk.meta.StepType;

public interface WerkConfig {
	//[ JobTypeName : JobType ]
	Map<String, JobType> getJobTypes();
	//[ JobTypeName : [ StepTypeName : StepType ] ]
	Map<String, Map<String, StepType>> getStepTypes();
}
