package org.werk.engine.local;

import org.werk.processing.jobs.JobToken;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@EqualsAndHashCode
public class LongToken implements JobToken {
	@Getter
	long value;
}
