package org.werk.data;

import java.util.List;
import java.util.Map;

import org.werk.processing.parameters.Parameter;
import org.werk.processing.steps.StepProcessingLogRecord;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class StepPOJOImpl implements StepPOJO {
	@Getter
	String stepTypeName;

	@Getter
	boolean isRollback;

	@Getter
	int stepNumber;

	@Getter
	List<Integer> rollbackStepNumbers;

	@Getter
	int executionCount;

	@Getter
	Map<String, Parameter> stepParameters;

	@Getter
	List<StepProcessingLogRecord> processingLog;
}
