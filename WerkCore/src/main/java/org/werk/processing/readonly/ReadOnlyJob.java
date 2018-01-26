package org.werk.processing.readonly;

import java.util.List;

import org.werk.data.JobPOJO;

public interface ReadOnlyJob extends JobPOJO {
	List<ReadOnlyStep> getProcessingHistory();
	List<ReadOnlyStep> getFilteredHistory(String stepType);
	ReadOnlyStep getStep(long stepNumber);
}
