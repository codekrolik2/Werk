package org.werk.engine;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.pillar.time.interfaces.TimeProvider;
import org.pillar.time.interfaces.Timestamp;
import org.werk.processing.jobs.Job;
import org.werk.processing.steps.callback.WerkCallback;
import org.werk.processing.steps.callback.WerkCallbackListener;

import lombok.AllArgsConstructor;
import lombok.Setter;

public class WerkCallbackRunnable<J> implements Runnable, WerkCallbackListener<String> {
	@AllArgsConstructor
	class CallbackWaitStruct implements Comparable<CallbackWaitStruct> {
		WerkCallback<String> callback;
		Job<J> job;
		long timeout;
		Timestamp registrationTime;
		String parameterName;

		Timestamp getTimeToRun(Timestamp currentTime) {
			if (registrationTime.compareTo(currentTime) > 0)
				registrationTime = currentTime;
			
			long passed = registrationTime.getDeltaInMs(currentTime);
			registrationTime = registrationTime.shiftBy(passed, TimeUnit.MILLISECONDS);
			timeout -= passed;
			
			return registrationTime.shiftBy(timeout, TimeUnit.MILLISECONDS);
		}
		
		@Override
		public int compareTo(WerkCallbackRunnable<J>.CallbackWaitStruct o) {
			Timestamp currentTime = timeProvider.getCurrentTime();
			return getTimeToRun(currentTime).compareTo(o.getTimeToRun(currentTime));
		}
	}
	
	@Setter
	protected WerkPool<J> pool;
	protected ReentrantLock lock;
	protected Condition condition;
	protected boolean isRunning;

	protected Map<WerkCallback<String>, CallbackWaitStruct> callbacks;
	protected PriorityQueue<CallbackWaitStruct> callbacksByTimeout;
	protected TimeProvider timeProvider;
	
	public WerkCallbackRunnable(TimeProvider timeProvider) {
		this.timeProvider = timeProvider;
		lock = new ReentrantLock();
		condition = lock.newCondition();
		isRunning = true;

		callbacks = new HashMap<>();
		callbacksByTimeout = new PriorityQueue<>();
	}

	@Override
	public void run() {
		lock.lock();
		try {
			while (isRunning) {
				try {
					if (callbacksByTimeout.isEmpty()) {
						condition.await();
					} else {
						Timestamp currentTime = timeProvider.getCurrentTime();
						Timestamp timeToRun = callbacksByTimeout.peek().getTimeToRun(currentTime);
						
						if (timeToRun.compareTo(currentTime) > 0) {
							condition.await(currentTime.getDeltaInMs(timeToRun), TimeUnit.MILLISECONDS);
						} else {
							CallbackWaitStruct cws = callbacksByTimeout.poll();
							resultReceived(cws.callback, null);
						}
					}
				} catch (InterruptedException e) {
					//Do nothing
				}
			}
		} finally {
			lock.unlock();
		}
	}

	public void addCallback(WerkCallback<String> callback, Job<J> job, Optional<Long> timeout, String parameterName) {
		lock.lock();
		try {
			callback.addListener(this);
			
			long timeoutL = timeout.isPresent() ? timeout.get() : 0;
			Timestamp registrationTime = timeProvider.getCurrentTime();
			
			CallbackWaitStruct cws = new CallbackWaitStruct(callback, job, timeoutL, registrationTime, parameterName);
			callbacks.put(callback, cws);
			
			if (timeout.isPresent()) {
				callbacksByTimeout.add(cws);
				condition.signal();
			}
			
			//If Q is 2x size of the Map, remove all nonexistent from Q
			//Takes Linear time since structs are added in order
			if (callbacksByTimeout.size()*2 < callbacks.size()) {
				PriorityQueue<CallbackWaitStruct> newCallbacksByTimeout = new PriorityQueue<>();
				
				while (!callbacksByTimeout.isEmpty()) {
					CallbackWaitStruct tmpCws = callbacksByTimeout.poll();
					if (callbacks.containsKey(tmpCws.callback))
						newCallbacksByTimeout.add(tmpCws);
				}
				
				callbacksByTimeout = newCallbacksByTimeout;
			}
		} finally {
			lock.unlock();
		}
	}
	
	public void shutdown() {
		lock.lock();
		try {
			isRunning = false;
			condition.signal();
		} finally {
			lock.unlock();
		}
	}
	
	@Override
	public void resultReceived(WerkCallback<String> caller, String result) {
		lock.lock();
		try {
			CallbackWaitStruct cws = callbacks.remove(caller);
			if (cws != null) {
				if (result != null)
					cws.job.getCurrentStep().putStringParameter(cws.parameterName, result);
				else
					cws.job.getCurrentStep().removeStepParameter(cws.parameterName);
					
				pool.addUnitOfWork(cws.job, 0);
			}
		} finally {
			lock.unlock();
		}
	}
}
