package org.werk.config;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.werk.meta.JobType;
import org.werk.meta.OverflowAction;
import org.werk.meta.inputparameters.JobInputParameter;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class JobTypeImpl implements JobType {
	@Getter
	protected String jobTypeName;
	@Getter
	List<String> stepTypes;
	@Getter
	protected Map<String, List<JobInputParameter>> initParameters;
	@Getter
	protected String firstStepTypeName;
	@Getter
	protected String description;
	@Getter
	protected String jobConfig;
	@Getter
	protected boolean forceAcyclic;
	@Getter
	protected long version;
	@Getter
	long historyLimit;
	@Getter
	OverflowAction historyOverflowAction;
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("JobType \"").append(jobTypeName).append("\" [Version ").append(version).append("]");
		if (forceAcyclic)
			sb.append(" [Acyclic]");
		else
			sb.append(" [Cycles allowed]");
		
		if ((jobConfig != null) && (!jobConfig.toString().trim().equals(""))) {
			sb.append("\nConfig:\n").append(jobConfig);
		}
		
		sb.append("\n");
		
		for (Entry<String, List<JobInputParameter>> ent : initParameters.entrySet()) {
			sb.append("Parameters SET ").append(ent.getKey()).append(":").append("\n");
			
			List<JobInputParameter> prmSet = ent.getValue();
			for (JobInputParameter prm : prmSet)
				sb.append("\t").append(prm.toString()).append("\n");
		}
		
		sb.append("Steps:").append("\n");
		for (String step : stepTypes) {
			sb.append("\t").append(step);
			if (step.equals(firstStepTypeName))
				sb.append(" (First)");
			
			sb.append("\n");
		}
		
		return sb.toString();
	}
}
