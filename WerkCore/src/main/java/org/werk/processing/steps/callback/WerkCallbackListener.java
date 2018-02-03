package org.werk.processing.steps.callback;

public interface WerkCallbackListener<V> {
	void resultReceived(WerkCallback<V> caller, V result);
}
