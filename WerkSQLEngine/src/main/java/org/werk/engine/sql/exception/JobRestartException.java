package org.werk.engine.sql.exception;

public class JobRestartException extends Exception {
	private static final long serialVersionUID = -6304172228059148037L;

	public JobRestartException() {
		super();
	}

	public JobRestartException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public JobRestartException(String message, Throwable cause) {
		super(message, cause);
	}

	public JobRestartException(String message) {
		super(message);
	}

	public JobRestartException(Throwable cause) {
		super(cause);
	}
}
