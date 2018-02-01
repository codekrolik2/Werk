package org.werk.exceptions;

public class StepLogLimitExceededException extends Exception {
	private static final long serialVersionUID = -1777987026728842700L;

	public StepLogLimitExceededException() {
		super();
	}

	public StepLogLimitExceededException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public StepLogLimitExceededException(String message, Throwable cause) {
		super(message, cause);
	}

	public StepLogLimitExceededException(String message) {
		super(message);
	}

	public StepLogLimitExceededException(Throwable cause) {
		super(cause);
	}
}
