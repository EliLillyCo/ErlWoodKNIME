/*
 * ------------------------------------------------------------------------
 *
 * Copyright (C) 2014 Eli Lilly and Company Limited
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * ------------------------------------------------------------------------
*/
package org.erlwood.knime.utils.jobhandling;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.knime.core.node.ExecutionContext;

/**
 * This class is designed to allow for job cancellation by the KNIME
 * ExecutionContext.
 * @author Luke Bullard
 *
 * @param <T> The return type for the job execution.
 */
public class CancellableJob<T> {
	/** Prefix for service thread names.	 */
	private final String threadPrefix;
	
	/**
	 * Simple job execution interface.
	 * @author Luke Bullard
	 *
	 * @param <T> The return type for the job execution.
	 */
	public interface Job<T> {
		/**
		 * Execute a job.
		 * @return T
		 * @throws Exception On Error
		 */
		T execute() throws Exception;
	};
	
	/**
	 * Constructor.
	 * @param threadPrefix The thread prefix to use.
	 */
	public CancellableJob(String threadPrefix) {
		this.threadPrefix = threadPrefix;
	}
	
	/**
	 * Execute a job, whilst checking for job cancellation via the checkCanceled
	 * method of ExecutionContext
	 * @param exec The ExecutionContext
	 * @param theJob The job to run
	 * @return T
	 * @throws Throwable On Error
	 */
	@SuppressWarnings("unchecked")
	public T execute(final ExecutionContext exec, final Job<T> theJob) throws Throwable {
		
		// initialise the thread pool
		final ExecutorService threadPool = Executors.newFixedThreadPool(
				2,
				new ThreadFactory( ) {

					@Override
					public Thread newThread(final Runnable runnable) {
						Thread thread = new Thread(runnable);
						thread.setName(threadPrefix + "_" + thread.getId( ));
						return thread;
						
					}
					
				}
		);
		CompletionService<Object> pool = new ExecutorCompletionService<Object>(threadPool);
		
		// create monitor thread for cancellation
		pool.submit(new Callable<Object>( ) {
			
			@Override
			public Void call( ) throws Exception {
				try {
					while(!threadPool.isShutdown( )) {
						// check for cancellation
						if (exec != null) {
							exec.checkCanceled( );
						}
						
						// wait before checking again for cancellation
						Thread.sleep(300);
					}
				} catch(InterruptedException e) {
					// interrupted when service thread completes
					Thread.currentThread( ).interrupt( );
				} finally {
					threadPool.shutdownNow( );
				}
				
				return null;
			}
			
		});
		
		// Job execution thread
		pool.submit(new Callable<Object>( ) {
			
			@Override
			public Object call( ) throws Exception {
				T result = null;
				try {
					result = theJob.execute();
				} finally {
					threadPool.shutdownNow( );
				}
				return result;
			}
			
		});
		
		// extract the result
		try {
			for(int i = 0; i < 2; i++) {
				Object o = pool.take( ).get( );
				if(o != null) {
					return (T)o;
				}
			}
			
			// Nothing was returned from either thread
			return null;
		} catch(ExecutionException e) {
			Throwable ex = e;
			if(e.getCause( ) != null) {
				// Only report the cause exception to the user
				ex = e.getCause( );
			}
			throw ex;			
		} 
	}
}
