package org.werk.engine;

public interface JobIdSerializer<J> {
	String serializeJobId(J jobId);
	J deSerializeJobId(String jobIdStr);
}
