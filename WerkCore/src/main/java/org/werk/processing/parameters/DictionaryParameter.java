package org.werk.processing.parameters;

import java.util.Map;

public interface DictionaryParameter extends Parameter {
	Map<String, Parameter> getValue();
}
