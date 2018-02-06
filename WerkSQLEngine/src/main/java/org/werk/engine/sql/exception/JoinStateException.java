package org.werk.engine.sql.exception;

public class JoinStateException extends Exception {
	private static final long serialVersionUID = -3881814593597164393L;

	public JoinStateException() {
		super();
	}

	public JoinStateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public JoinStateException(String message, Throwable cause) {
		super(message, cause);
	}

	public JoinStateException(String message) {
		super(message);
	}

	public JoinStateException(Throwable cause) {
		super(cause);
	}
}
