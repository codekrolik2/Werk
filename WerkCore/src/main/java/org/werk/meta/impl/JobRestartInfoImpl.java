package org.werk.meta.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.werk.meta.JobRestartInfo;
import org.werk.meta.NewStepRestartInfo;
import org.werk.processing.jobs.JoinStatusRecord;
import org.werk.processing.parameters.Parameter;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class JobRestartInfoImpl<J> implements JobRestartInfo<J> {
	@Getter
	J jobId;
	@Getter
	Map<String, Parameter> jobParametersUpdate;
	@Getter
	List<String> jobParametersToRemove;
	@Getter
	Map<String, Parameter> stepParametersUpdate;
	@Getter
	List<String> stepParametersToRemove;
	@Getter
	Optional<NewStepRestartInfo> newStepInfo;
	@Getter
	Optional<JoinStatusRecord<J>> joinStatusRecord;
}
