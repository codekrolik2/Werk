package org.werk.engine.sql.DAO;

import java.util.List;
import java.util.Map;

import org.werk.data.StepPOJO;
import org.werk.processing.parameters.Parameter;
import org.werk.processing.steps.StepProcessingLogRecord;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class DBStepPOJO implements StepPOJO {
	@Getter
	long jobId;
	@Getter
	long stepId;
	@Getter
	protected String stepTypeName;
	@Getter
	protected boolean isRollback;
	@Getter
	protected int stepNumber;
	@Getter
	protected List<Integer> rollbackStepNumbers;
	@Getter
	protected int executionCount;
	@Getter
	protected Map<String, Parameter> stepParameters;
	@Getter
	protected List<StepProcessingLogRecord> processingLog;
}
