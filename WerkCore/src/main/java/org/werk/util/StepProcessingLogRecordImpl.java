package org.werk.util;

import org.pillar.time.interfaces.Timestamp;
import org.werk.processing.steps.StepProcessingLogRecord;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class StepProcessingLogRecordImpl implements StepProcessingLogRecord {
	@Getter
	protected Timestamp time;
	@Getter
	protected String message;
}
