package org.werk.config;

import org.werk.exceptions.WerkConfigException;

public interface WerkConfigLoader<J> {
	WerkConfig<J> loadWerkConfig() throws WerkConfigException;
	WerkConfig<J> loadWerkConfig(Object[] loadParameters) throws WerkConfigException;
}
