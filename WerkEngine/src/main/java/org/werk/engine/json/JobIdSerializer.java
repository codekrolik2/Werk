package org.werk.engine.json;

public interface JobIdSerializer<J> {
	String serializeJobId(J jobId);
	J deSerializeJobId(String jobIdStr);
}
