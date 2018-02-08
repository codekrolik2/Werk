package org.werk.engine.sql.exception;

public class SQLStepSwitcherException extends RuntimeException {
	private static final long serialVersionUID = -8911240335712801424L;

	public SQLStepSwitcherException() {
		super();
	}

	public SQLStepSwitcherException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public SQLStepSwitcherException(String message, Throwable cause) {
		super(message, cause);
	}

	public SQLStepSwitcherException(String message) {
		super(message);
	}

	public SQLStepSwitcherException(Throwable cause) {
		super(cause);
	}
}