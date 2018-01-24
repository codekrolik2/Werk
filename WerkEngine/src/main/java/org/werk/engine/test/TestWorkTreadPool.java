package org.werk.engine.test;

import org.pillar.exec.work.WorkThreadPool;
import org.pillar.exec.work.WorkThreadPoolRunnable;
import org.pillar.time.LongTimeProvider;
import org.pillar.time.interfaces.TimeProvider;

public class TestWorkTreadPool {
	static class MyPool extends WorkThreadPool<String> {
		public MyPool(int size, TimeProvider timeProvider) {
			super(size, timeProvider);
		}
		
		@Override
		protected WorkThreadPoolRunnable<String> createRunnable() {
			return new MyRunnable(this);
		}
	}
	
	static class MyRunnable extends WorkThreadPoolRunnable<String> {
		public MyRunnable(WorkThreadPool<String> pool) {
			super(pool);
		}
		
		@Override
		public void process(String work) {
			System.out.println(work);
		}
	}
	
	public static void main(String[] args) {
		TimeProvider timeProvider = new LongTimeProvider();
		
		MyPool pool = new MyPool(4, timeProvider);
		
		for (int i = 0; i < 1000; i++)
			pool.addUnitOfWork("0 " + i, 0);
		
		for (int i = 0; i < 1000; i++)
			pool.addUnitOfWork("1000 " + i, 1000);
		
		for (int i = 0; i < 1000; i++)
			pool.addUnitOfWork("5000 " + i, 5000);
		
		for (int i = 0; i < 1000; i++)
			pool.addUnitOfWork("10000 " + i, 10000);
		
		//pool.shutdown();
	}
}
