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
package org.erlwood.knime.utils.clients;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.erlwood.knime.WebServiceCoreActivator;
import org.erlwood.knime.WebServicePreferenceInitializer;
import org.erlwood.knime.utils.exceptions.NotAuthorisedException;
import org.erlwood.knime.utils.exceptions.NotFoundException;
import org.erlwood.knime.utils.exceptions.WebServiceException;
import org.erlwood.knime.utils.iotiming.IOTiming;
import org.erlwood.knime.utils.jobhandling.CancellableJob;
import org.erlwood.knime.utils.jobhandling.CancellableJob.Job;
import org.erlwood.knime.utils.settings.WebServiceSettings;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.NodeLogger;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

/** Super class for the Web Service Client implementations containing common functions. Specifically
 * the method for allowing cancellation of web service executions.
 * @author Tom Wilkin */
public abstract class WebServiceClient {
	
	/** The node logger instance. */
	private static final NodeLogger LOGGER = NodeLogger.getLogger(WebServiceClient.class);
	
	/** The id of the thread that created this instance to check as this class is not thread safe.*/
	private final long THREAD_ID;
		
	/** The list of headers that should be appended to outgoing requests. */
	protected Map<String, List<String>> headers;
	
	/** Can we compress ? **/
	private boolean canCompress;

	private IOTiming ioTiming;	

	/** The web service settings from the last execution call. */
	protected WebServiceSettings settings;	
	//required for testing
	protected WebServiceClient (){
		THREAD_ID = Thread.currentThread().getId();
	}
	
	protected WebServiceClient(final IOTiming timingInterface, final WebServiceSettings settings) {
		this.ioTiming = timingInterface;
		setTiming(0);
		
		// store the thread id for this client
		THREAD_ID = Thread.currentThread( ).getId( );	
		
		//	Store the settings
		this.settings = settings;
				
		// initialise the HTTP headers
		headers = new HashMap<String, List<String>>();
	}
	
	
	protected void setTiming(long t) {
		if (ioTiming != null) {
			ioTiming.setTiming(t);
		}
	}
	
	protected void addTiming(long t) {
		if (ioTiming != null) {
			ioTiming.addTiming(t);
		}
	}
	
	/** 
	 * @return The web service settings
	 */
	public WebServiceSettings getSettings() {
		return settings;
	}
	
	/** Add a header to the HTTP requests
	 * @param name The name of the header to add.
	 * @param value The value of the header to add. */
	public void addHeader(final String name, final String value) {
		List<String> lst = headers.get(name);
		if(lst == null) {
			lst = new ArrayList<String>();
			headers.put(name, lst);
		}
		if (!lst.contains(value)) {			
			lst.add(value);
		} 
	}
	
	/** Replace a header in the HTTP request.
	 * @param name The name of the header to replace.
	 * @param value The value of the header to replace. */
	public void replaceHeader(final String name, final String value) {
		List<String> lst = new ArrayList<String>( );
		lst.add(value);
		headers.put(name, lst);
	}
	
	/** Invoke the web service method and return the results. Only to be called from a NodeDialog
	 * as it is not possible to cancel after it has started.
	 * @param method The method on the web service to invoke.
	 * @param parameters The parameters to pass to the web service method.
	 * @return The result of the web service method execution.
	 * @throws WebServiceException If an error occurs invoking the method on the web service. */
	public abstract Object[ ] invoke(final String method, final Object... parameters) 
			throws WebServiceException;
	
	/** Invoke the web service method and return the results.
	 * @param exec The node execution context used to check for node cancellation.
	 * @param method The method on the web service to invoke.
	 * @param parameters The parameters to pass to the web service method.
	 * @return The result of the web service method execution.
	 * @throws CanceledExecutionException If the user cancels the execution while the web service 
	 * 		call is still running.
	 * @throws WebServiceException If an error occurs invoking the method on the web service. */
	public Object[ ] invoke(final ExecutionContext exec, final String method, 
			final Object... parameters) throws CanceledExecutionException, WebServiceException 
	{
		// ensure the web service client is operating in the correct thread
		checkThread( );
		
		// Extract the result
		try {
			CancellableJob<Object[]> canJob = new CancellableJob<Object[]>("WSClient_" + method);
			
			return canJob.execute(exec, new Job<Object[]>() {

				@Override
				public Object[] execute() throws Exception {					
					return invoke(method, parameters);
				}
				
			});
			
		} catch(Throwable ex) {
			if(ex instanceof InterruptedException) {
				throw new WebServiceException("Thread was interrupted.", ex);
			} else if(ex instanceof CanceledExecutionException) {
				throw (CanceledExecutionException)ex;
			} else if(ex instanceof WebServiceException) {
				throw (WebServiceException)ex;
			} else {
				throw createDefaultException(method, ex);
			}
		}
		
	}

	/** Retrieve the user specified time out from the KNIME preferences.
	 * @return The time out value to use for the web service calls. */
	public static int getPreferenceTimeOut( ) {
		int timeOut = WebServicePreferenceInitializer.getInt(
				WebServiceCoreActivator.WEB_SERVICE_TIMEOUT
		);
		if(timeOut <= 0) {
			timeOut = WebServicePreferenceInitializer.getDefaultInt(
					WebServiceCoreActivator.WEB_SERVICE_TIMEOUT
			);
			LOGGER.warn("Web service time out value must be a positive integer greater than zero.");
		}
		return timeOut;
	}
	
	/** Retrieve the user specified max child elements from the KNIME preferences.
	 * @return The max child elements for the web service calls. */
	public static int getPreferenceMaxChildElements( ) {
		int maxChildElements = WebServicePreferenceInitializer.getInt(
				WebServiceCoreActivator.WEB_SERVICE_MAX_CHILD_ELEMENTS
		);
		if(maxChildElements <= 0) {
			maxChildElements = WebServicePreferenceInitializer.getDefaultInt(
					WebServiceCoreActivator.WEB_SERVICE_MAX_CHILD_ELEMENTS
			);
		}
		return maxChildElements;
	}
	
	/** Check the response for common error codes.
	 * @param statusCode The status code to check for common errors.
	 * @throws WebServiceException If the status code indicated there was an error. */
	protected void checkForErrors(final int statusCode) throws WebServiceException {
		switch(statusCode) {
			case 200:
				return;
			case 401:
			case 403:
				throw new NotAuthorisedException( );
			case 404:
				throw new NotFoundException( );
			default:
				throw new WebServiceException("Server returned code " + statusCode);
		}
	}
	
	/** Create an exception warning the user that the URL is invalid.
	 * @param uri The URI that was invalid.
	 * @return The exception to warn the user. */
	protected WebServiceException createDefaultException(final URI uri) {
		return new WebServiceException(
				"Could not create web service client from URL '" + uri + "'."
		);
	}
	
	/** Create an exception warning the user that the web service method could not be invoked.
	 * @param method The web service method that was to be invoked.
	 * @param t The throwable generated when trying to invoke that web service method.
	 * @return The exception to warn the user. */
	public WebServiceException createDefaultException(final String method, final Throwable t) {
		String message;
		if(method != null && !"".equals(method)) {
			message = "Could not invoke '" + method + "' on web service. ";
		} else {
			message = "Could not invoke web service.";
		}
		
		if(t.getMessage( ) != null) {
			message += t.getMessage( );
		}
		
		return new WebServiceException(message,	t);
	}
	
	/** Create an exception informing the user that they can change one of the settings properties
	 * to resolve the issue.
	 * @param message The message to prepend on the standard text.
	 * @param t The throwable describing the original issue.
	 * @return The exception to inform the user. */
	protected WebServiceException createSettingsException(final String message, 
			final Throwable t) 
	{
		return new WebServiceException(
				message + " Try increasing it, by changing the value stored in the node"
						+ " configure dialog 'Web Service' tab.",
				t
		);
	}
	
	/** Retrieve the proxy settings from the Eclipse configuration.
	 * @param uri The URI to retrieve the proxy configuration for.
	 * @return The proxy configuration (if any) or null. */
	protected URI retrieveProxy(final URI uri) {
		ServiceTracker<IProxyService, ?> tracker = null;
		try {
			tracker = new ServiceTracker<IProxyService, String>(
					FrameworkUtil.getBundle(getClass( )).getBundleContext( ), 
					IProxyService.class.getName( ), 
					null
			);
			tracker.open( );
		
			IProxyService proxy = (IProxyService)tracker.getService( );
			IProxyData[ ] proxyData = proxy.select(uri);
			for(IProxyData data : proxyData) {
				if(data.getHost( ) != null) {
					URI proxyURI = new URI("http", null, data.getHost( ), data.getPort( ), 
							null, null, null
					);
					LOGGER.debug("Using proxy '" + proxyURI + "'.");
					return proxyURI;
				}
			}
		} catch(Exception e) { 
			LOGGER.error(e);
		} finally {
			if(tracker!=null){
				tracker.close( );
			}
		}
		
		return null;
	}
	
	/** Method to ensure the web service client is operating in only one thread.
	 * @throws WebServiceException If the web service client operates in a different thread. */
	private void checkThread( ) throws WebServiceException {
		if(THREAD_ID != Thread.currentThread( ).getId( )) {
			throw new WebServiceException(
					"Cannot use the same web service client in multiple threads."
			);
		}
	}
	
	
	/**
	 * Can the client compress the payload or not ?
	 * @param b TRUE if you want to allow gzip compression to be used (subject to a 1024 byte floor) 
	 */
	public void canCompress(boolean b) {
		this.canCompress = b;
	}

	/** 
	 * @return TRUE if you want to allow gzip compression to be used (subject to a 1024 byte floor)
	 */
	protected boolean canCompress() {
		return canCompress;
	}
}
