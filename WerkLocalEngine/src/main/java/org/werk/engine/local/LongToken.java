package org.werk.engine.local;

import org.werk.processing.jobs.JobToken;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class LongToken implements JobToken {
	@Getter
	long value;
}
