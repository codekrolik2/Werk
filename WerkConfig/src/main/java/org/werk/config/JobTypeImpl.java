package org.werk.config;

import java.util.List;

import org.werk.meta.JobType;
import org.werk.meta.inputparameters.JobInputParameter;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class JobTypeImpl implements JobType {
	@Getter
	protected String jobTypeName;
	@Getter
	protected List<List<JobInputParameter>> initInfo;
	@Getter
	protected String firstStepTypeName;
	@Getter
	protected String description;
	@Getter
	protected String customInfo;
	@Getter
	protected boolean forceAcyclic;
}
