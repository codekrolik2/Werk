package org.werk.restclient;

public interface Callback<R> {
	void done(R result);
	void error(Throwable cause);
}
