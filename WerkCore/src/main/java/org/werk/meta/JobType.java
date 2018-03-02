package org.werk.meta;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.werk.meta.inputparameters.JobInputParameter;

public interface JobType extends JobTypeSignature {
	Map<String, List<JobInputParameter>> getInitParameters();
	
	String getFirstStepTypeName();
	Set<String> getStepTypes();
	
	//-------------------------
	
	String getDescription();
	
	String getJobConfig();
	boolean isForceAcyclic();
	
	long getHistoryLimit();
	OverflowAction getHistoryOverflowAction();
}
