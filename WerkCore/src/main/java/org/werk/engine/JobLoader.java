package org.werk.engine;

import java.util.Map;

import org.werk.data.JobPOJO;
import org.werk.data.StepPOJO;

public interface JobLoader {
	Map<JobPOJO, StepPOJO> loadJobsAndCurrentSteps();
}
