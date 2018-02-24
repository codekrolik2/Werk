package org.werk.restclient;

public interface WerkCallback<R> {
	void done(R result);
	void error(Throwable cause);
}
