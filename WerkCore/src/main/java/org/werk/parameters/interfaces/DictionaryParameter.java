package org.werk.parameters.interfaces;

import java.util.Map;

public interface DictionaryParameter extends Parameter {
	Map<String, Parameter> getValue();
}
