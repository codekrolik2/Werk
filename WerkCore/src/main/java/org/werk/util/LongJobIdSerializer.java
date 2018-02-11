package org.werk.util;

import org.werk.engine.JobIdSerializer;

public class LongJobIdSerializer implements JobIdSerializer<Long> {
	@Override
	public String serializeJobId(Long jobId) {
		return jobId.toString();
	}

	@Override
	public Long deSerializeJobId(String jobIdStr) {
		return Long.parseLong(jobIdStr);
	}
}
