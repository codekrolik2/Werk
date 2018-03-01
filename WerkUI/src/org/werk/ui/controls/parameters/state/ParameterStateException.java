package org.werk.ui.controls.parameters.state;

public class ParameterStateException extends Exception {
	private static final long serialVersionUID = -8028917929343804838L;

	public ParameterStateException() {
		super();
	}

	public ParameterStateException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ParameterStateException(String message, Throwable cause) {
		super(message, cause);
	}

	public ParameterStateException(String message) {
		super(message);
	}

	public ParameterStateException(Throwable cause) {
		super(cause);
	}
}
