package org.werk.config;

import org.werk.exceptions.WerkConfigException;

public interface WerkConfigLoader {
	WerkConfig loadWerkConfig() throws WerkConfigException;
}
