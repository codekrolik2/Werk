package org.werk.meta;

import java.util.Map;
import java.util.Optional;

import org.pillar.time.interfaces.Timestamp;
import org.werk.processing.parameters.Parameter;

public interface VersionJobInitInfo {
	String getJobTypeName();
	Optional<String> getInitSignatureName();
	Map<String, Parameter> getInitParameters();
	long getJobVersion();
	Optional<String> getJobName();
	Optional<Timestamp> getNextExecutionTime();
}
