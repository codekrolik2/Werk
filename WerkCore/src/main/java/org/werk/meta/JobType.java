package org.werk.meta;

import java.util.List;

import org.werk.meta.inputparameters.JobInputParameter;

public interface JobType {
	String getJobTypeName();
	long getVersion();
	
	List<List<JobInputParameter>> getInitParameters();
	
	String getFirstStepTypeName();
	List<String> getStepTypes();

	//-------------------------
	
	String getDescription();
	
	String getCustomInfo();
	boolean isForceAcyclic();
}
