package org.werk.ui.controls.jobsform;

import java.util.Date;
import java.util.Optional;

import org.pillar.time.LongTimestamp;
import org.werk.data.JobPOJO;
import org.werk.meta.JobTypeSignature;
import org.werk.processing.jobs.JobStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class TableJobPOJO<J> {
	@Getter
	JobPOJO<J> jobPOJO;
	
	public J getJobId() {
		return jobPOJO.getJobId();
	}
	
	public JobStatus getStatus() {
		return jobPOJO.getStatus();
	}
	
	public String getJobName() {
		return jobPOJO.getJobName().isPresent() ? jobPOJO.getJobName().get() : "";
	}
	
	public String getJobType() {
		return JobTypeSignature.getJobTypeFullName(jobPOJO.getJobTypeName(), jobPOJO.getVersion());
	}
	
	public String getStepType() {
		return String.format("%s [#%d]", jobPOJO.getCurrentStepTypeName(), jobPOJO.getStepCount());
	}
	
	public Optional<J> getParentJobId() {
		return jobPOJO.getParentJobId();
	}
	
	public Date getCreationTime() {
		LongTimestamp longTs = ((LongTimestamp)jobPOJO.getCreationTime());
		return new Date(longTs.getUnixTime());
	}
	
	public Date getNextExecutionTime() {
		LongTimestamp longTs = ((LongTimestamp)jobPOJO.getNextExecutionTime());
		return new Date(longTs.getUnixTime());
	}
}
