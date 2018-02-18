package org.werk.processing.readonly;

import java.util.Collection;

import org.werk.data.JobPOJO;
import org.werk.data.StepPOJO;

public interface ReadOnlyJob<J> extends JobPOJO<J> {
	Collection<StepPOJO> getProcessingHistory() throws Exception;
	Collection<StepPOJO> getFilteredHistory(String stepType) throws Exception;
	StepPOJO getStep(int stepNumber) throws Exception;
}
