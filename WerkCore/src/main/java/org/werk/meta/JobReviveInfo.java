package org.werk.meta;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.werk.processing.parameters.Parameter;

public interface JobReviveInfo<J> {
	J getJobId();
	
	Map<String, Parameter> getJobParametersUpdate();
	List<String> getJobParametersToRemove();
	
	Map<String, Parameter> getStepParametersUpdate();
	List<String> getStepParametersToRemove();
	
	Optional<String> getNewStepTypeName();
	Optional<Boolean> isNewStepRollback();
	List<Integer> getStepsToRollback();
}
