package org.werk.engine.sql;

import java.util.List;
import java.util.Map;

import org.pillar.time.interfaces.TimeProvider;
import org.werk.engine.processing.WerkStep;
import org.werk.meta.StepType;
import org.werk.processing.jobs.Job;
import org.werk.processing.parameters.Parameter;
import org.werk.processing.steps.StepExec;
import org.werk.processing.steps.StepProcessingLogRecord;
import org.werk.processing.steps.Transitioner;

import lombok.Getter;

public class SQLWerkStep extends WerkStep<Long> {
	@Getter
	long stepId;

	public SQLWerkStep(Job<Long> job, StepType<Long> stepType, boolean isRollback, int stepNumber,
			List<Integer> rollbackStepNumbers, int executionCount, Map<String, Parameter> stepParameters,
			List<StepProcessingLogRecord> processingLog, StepExec<Long> stepExec, Transitioner<Long> stepTransitioner,
			TimeProvider timeProvider, long stepId) {
		super(job, stepType, isRollback, stepNumber, rollbackStepNumbers, executionCount, stepParameters, processingLog,
				stepExec, stepTransitioner, timeProvider);
		this.stepId = stepId;
	}
}
