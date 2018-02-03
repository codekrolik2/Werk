package org.werk.processing.steps.callback;

public class ResultAlreadySetException extends Exception {
	private static final long serialVersionUID = 6005357023489371023L;

	public ResultAlreadySetException() {
		super();
	}

	public ResultAlreadySetException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ResultAlreadySetException(String message, Throwable cause) {
		super(message, cause);
	}

	public ResultAlreadySetException(String message) {
		super(message);
	}

	public ResultAlreadySetException(Throwable cause) {
		super(cause);
	}
}
