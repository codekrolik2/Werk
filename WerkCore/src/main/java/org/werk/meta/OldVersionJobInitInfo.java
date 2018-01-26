package org.werk.meta;

import java.util.Map;
import java.util.Optional;

import org.werk.processing.parameters.Parameter;

public interface OldVersionJobInitInfo {
	String getJobTypeName();
	Map<String, Parameter> getInitParameters();
	long getOldVersion();
	Optional<String> getJobName();
}
