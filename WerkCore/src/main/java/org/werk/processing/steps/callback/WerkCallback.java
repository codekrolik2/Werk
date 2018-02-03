package org.werk.processing.steps.callback;

import java.util.concurrent.locks.ReentrantLock;

import lombok.AllArgsConstructor;

public interface WerkCallback<V> {
	/**
	 * Set result of operation. Will notify all registered listeners.
	 * Can only be called once.
	 * 
	 * @param result Result of operation
	 * @throws ResultAlreadySetException Will throw if result was already set
	 */
	void setResult(V result) throws ResultAlreadySetException;
	
	/**
	 * Add listener. Its method "resultReceived(...)" will be called when operation result will be set.
	 * If result was set prior to this call, "resultReceived(...)" will be called immediately
	 * 
	 * @param listener Werk callback listener
	 */
	void addListener(WerkCallbackListener<V> listener);
	
	static <V> WerkCallback<V> createCallback() {
		return new WerkCallback<V>() {
			@AllArgsConstructor
			class Listener {
				WerkCallbackListener<V> wcl;
				Listener next;
			}
			
			protected ReentrantLock lock = new ReentrantLock();
			protected Listener head = null;
			protected V result = null;
			
			@Override
			public void setResult(V result) throws ResultAlreadySetException {
				lock.lock();
				try {
					if (this.result == null) {
						this.result = result;
						
						while (head != null) {
							head.wcl.resultReceived(this, result);
							head = head.next;
						}
					} else
						throw new ResultAlreadySetException();
				} finally {
					lock.unlock();
				}
			}

			@Override
			public void addListener(WerkCallbackListener<V> listener) {
				lock.lock();
				try {
					if (this.result != null) {
						listener.resultReceived(this, result);
					} else {
						Listener l = new Listener(listener, head);
						head = l;
					}
				} finally {
					lock.unlock();
				}
			}
		};
	}
}
