package org.werk.processing.steps;

import org.pillar.time.interfaces.Timestamp;

public interface StepProcessingLogRecord {
	Timestamp getTime();
	String getMessage();
}
