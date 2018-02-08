package org.werk.engine.sql.exception;

public class SQLWerkPoolRunnableException extends RuntimeException {
	private static final long serialVersionUID = -4057354655703165363L;

	public SQLWerkPoolRunnableException() {
		super();
	}

	public SQLWerkPoolRunnableException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public SQLWerkPoolRunnableException(String message, Throwable cause) {
		super(message, cause);
	}

	public SQLWerkPoolRunnableException(String message) {
		super(message);
	}

	public SQLWerkPoolRunnableException(Throwable cause) {
		super(cause);
	}
}
