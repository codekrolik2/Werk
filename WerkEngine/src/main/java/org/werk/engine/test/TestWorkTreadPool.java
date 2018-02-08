package org.werk.engine.test;

import org.pillar.exec.work.WorkThreadPool;
import org.pillar.exec.work.WorkThreadPoolRunnable;
import org.pillar.exec.work.WorkThreadPoolRunnableFactory;
import org.pillar.log4j.Log4JUtils;
import org.pillar.time.LongTimeProvider;
import org.pillar.time.interfaces.TimeProvider;

import lombok.Setter;

public class TestWorkTreadPool {
	static class MyRunnableFactory implements WorkThreadPoolRunnableFactory<String> {
		@Setter
		WorkThreadPool<String> pool;
		
		@Override
		public WorkThreadPoolRunnable<String> createRunnable() {
			return new MyRunnable(pool);
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
		Log4JUtils.debugInitLog4j();
		
		TimeProvider timeProvider = new LongTimeProvider();
		
		MyRunnableFactory myFactory = new MyRunnableFactory();
		WorkThreadPool<String> pool = new WorkThreadPool<String>(timeProvider, myFactory);
		myFactory.setPool(pool);
		pool.start(4);
		
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
