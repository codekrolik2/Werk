package org.werk.meta;

import java.util.List;
import java.util.Map;

import org.werk.meta.inputparameters.JobInputParameter;

public interface JobType {
	String getJobTypeName();
	long getVersion();
	
	Map<String, List<JobInputParameter>> getInitParameters();
	
	String getFirstStepTypeName();
	List<String> getStepTypes();

	//-------------------------
	
	String getDescription();
	
	String getJobConfig();
	boolean isForceAcyclic();

	long getHistoryLimit();
	OverflowAction getHistoryOverflowAction();
}
