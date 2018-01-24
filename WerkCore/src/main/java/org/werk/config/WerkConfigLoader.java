package org.werk.config;

import org.werk.config.WerkConfigException;

public interface WerkConfigLoader {
	WerkConfig loadWerkConfig() throws WerkConfigException;
}
