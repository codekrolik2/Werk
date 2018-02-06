package org.werk.engine.sql.exception;

public class JobReviveException extends Exception {
	private static final long serialVersionUID = -6304172228059148037L;

	public JobReviveException() {
		super();
	}

	public JobReviveException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public JobReviveException(String message, Throwable cause) {
		super(message, cause);
	}

	public JobReviveException(String message) {
		super(message);
	}

	public JobReviveException(Throwable cause) {
		super(cause);
	}
}
