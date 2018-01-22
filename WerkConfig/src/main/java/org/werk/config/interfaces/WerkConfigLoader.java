package org.werk.config.interfaces;

import org.werk.config.WerkConfigException;

public interface WerkConfigLoader {
	WerkConfig loadWerkConfig() throws WerkConfigException;
}
