package org.werk.exceptions;

public class WerkException extends Exception {
	private static final long serialVersionUID = 1193023912443930748L;

	public WerkException() {
		super();
	}

	public WerkException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public WerkException(String message, Throwable cause) {
		super(message, cause);
	}

	public WerkException(String message) {
		super(message);
	}

	public WerkException(Throwable cause) {
		super(cause);
	}
}
