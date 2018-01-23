package org.werk.engine;

public class WerkParameterException extends Exception {
	private static final long serialVersionUID = -8903417113410948313L;

	public WerkParameterException() {
		super();
	}

	public WerkParameterException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public WerkParameterException(String message, Throwable cause) {
		super(message, cause);
	}

	public WerkParameterException(String message) {
		super(message);
	}

	public WerkParameterException(Throwable cause) {
		super(cause);
	}
}