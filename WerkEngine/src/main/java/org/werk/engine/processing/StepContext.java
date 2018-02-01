package org.werk.engine.processing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pillar.time.interfaces.TimeProvider;
import org.werk.engine.json.ParameterUtils;
import org.werk.engine.json.StepProcessingLogRecordImpl;
import org.werk.exceptions.StepLogLimitExceededException;
import org.werk.meta.OverflowAction;
import org.werk.meta.StepType;
import org.werk.processing.parameters.Parameter;
import org.werk.processing.steps.StepProcessingLogRecord;

public class StepContext<J> extends ParameterContext {
	protected StepType<J> stepType;
	protected long executionCount;
	protected List<StepProcessingLogRecord> processingLog;
	protected TimeProvider timeProvider;

	public StepContext<J> cloneContext() {
		long executionCount0 = executionCount;
		
		Map<String, Parameter> stepParameters0 = new HashMap<>();
		for (Map.Entry<String, Parameter> stepParameter : parameters.entrySet())
			stepParameters0.put(stepParameter.getKey(), ParameterUtils.cloneParameter(stepParameter.getValue()));
		List<StepProcessingLogRecord> processingLog0 = new ArrayList<StepProcessingLogRecord>(processingLog);
		
		return new StepContext<J>(stepType, timeProvider, executionCount0, stepParameters0, processingLog0);
	}
	
	public StepContext(StepType<J> stepType, TimeProvider timeProvider, long executionCount, Map<String, Parameter> stepParameters, 
			List<StepProcessingLogRecord> processingLog) {
		super(stepParameters);
		this.executionCount = executionCount;
		this.processingLog = processingLog;
		this.timeProvider = timeProvider;
		this.stepType = stepType;
	}
	
	public long getExecutionCount() {
		return executionCount;
	}
	
	public long incrementExecutionCount() {
		return ++executionCount;
	}
	
	public List<StepProcessingLogRecord> getProcessingLog() {
		return Collections.unmodifiableList(processingLog);
	}
	
	public void appendToProcessingLog(String message) throws StepLogLimitExceededException {
		long logLimit = stepType.getLogLimit();
		if (processingLog.size() >= logLimit) {
			OverflowAction action = stepType.getLogOverflowAction();
			if (action == OverflowAction.FAIL) {
				throw new StepLogLimitExceededException(String.format("Step Log Limit reached [%d]", logLimit));
			} else {
				processingLog.remove(0);
			}
		}
		
		processingLog.add(new StepProcessingLogRecordImpl(timeProvider.getCurrentTime(), message));
	}
}
