package org.werk.exceptions;

public class WerkConfigException extends Exception {
	private static final long serialVersionUID = -8100988101758300479L;

	public WerkConfigException() {
		super();
	}

	public WerkConfigException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public WerkConfigException(String message, Throwable cause) {
		super(message, cause);
	}

	public WerkConfigException(String message) {
		super(message);
	}

	public WerkConfigException(Throwable cause) {
		super(cause);
	}
}