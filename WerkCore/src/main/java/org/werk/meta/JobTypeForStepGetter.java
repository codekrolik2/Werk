package org.werk.meta;

import java.util.Collection;

public interface JobTypeForStepGetter {
	Collection<JobType> getJobTypesForStep(String stepTypeName);
}
