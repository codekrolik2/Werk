package org.werk.meta;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.werk.processing.jobs.JoinStatusRecord;
import org.werk.processing.parameters.Parameter;

public interface JobRestartInfo<J> {
	J getJobId();
	
	Map<String, Parameter> getJobInitParametersUpdate();
	List<String> getJobInitParametersToRemove();
	
	Map<String, Parameter> getJobParametersUpdate();
	List<String> getJobParametersToRemove();
	
	Map<String, Parameter> getStepParametersUpdate();
	List<String> getStepParametersToRemove();
	
	Optional<NewStepRestartInfo> getNewStepInfo();
	
	Optional<JoinStatusRecord<J>> getJoinStatusRecord();
}
