package org.werk.meta;

import java.util.List;

import org.werk.meta.inputparameters.JobInputParameter;

public interface JobType {
	String getJobTypeName();
	List<List<JobInputParameter>> getInitInfo();
	String getFirstStepTypeName();

	//-------------------------
	
	String getDescription();
	
	String getCustomInfo();
	boolean isForceAcyclic();
}
