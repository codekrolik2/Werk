package org.werk.processing.readonly;

import java.util.List;

import org.werk.data.JobPOJO;
import org.werk.data.StepPOJO;

public interface ReadOnlyJob<J> extends JobPOJO<J> {
	List<StepPOJO> getProcessingHistory();
	List<StepPOJO> getFilteredHistory(String stepType);
	StepPOJO getStep(long stepNumber);
}
