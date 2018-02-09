package org.werk.engine.sql.jobload;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

public class SQLJobLoaderRunnable implements Runnable {
	final Logger logger = Logger.getLogger(SQLJobLoader.class);
	
	protected ReentrantLock lock;
	protected Condition condition;
	protected boolean isRunning;
	protected SQLJobLoader sqlJobLoader;
	
	protected long loadPeriodMS;

	public SQLJobLoaderRunnable(SQLJobLoader sqlJobLoader, long loadPeriodMS) {
		lock = new ReentrantLock();
		condition = lock.newCondition();
		isRunning = true;
		this.sqlJobLoader = sqlJobLoader;
		
		this.loadPeriodMS = loadPeriodMS;
	}

	@Override
	public void run() {
		lock.lock();
		try {
			while (isRunning) {
				long startTime = System.currentTimeMillis();
				try {
					sqlJobLoader.loadJobs(startTime, loadPeriodMS);
				} catch (Exception e1) {
					logger.error("Load jobs error", e1);
				}
				
				long currentTime = System.currentTimeMillis();
				long sleepTime = (startTime + loadPeriodMS) - currentTime;
				if (sleepTime < 0)
					sleepTime = 0;
				
				try {
					condition.await(sleepTime, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) { }
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
}
