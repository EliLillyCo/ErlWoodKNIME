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

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.erlwood.knime.utils.auth.AuthenticationUtils;
import org.erlwood.knime.utils.exceptions.WebServiceException;
import org.erlwood.knime.utils.iotiming.IOTiming;
import org.erlwood.knime.utils.settings.WebServiceSettings;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.workflow.CredentialsProvider;
import org.knime.core.node.workflow.FlowVariable;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.util.Base64;

/** Class used to ensure the interactions between REST clients and web services follow
 * a certain procedure giving a better consistency for the end user.
 * @author Tom Wilkin */
public abstract class RESTWebServiceClient extends WebServiceClient {
	private static final DateFormat ISO_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
	
	static {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        ISO_FORMAT.setTimeZone(tz);
    }
	
	/** Logger **/
	private static final NodeLogger LOG = NodeLogger.getLogger(RESTWebServiceClient.class);
	
	/** A Parameter to append to the REST GET query. */
	public static final class RESTParameter <T> {
		
		/** The name of the parameter. */
		private String name;
		
		/** The value of the parameter. */
		private T value;
		
		public RESTParameter(final String name, final T value) {
			this.name = name;
			this.value = value;
		}
		
		/** @return The name of this parameter. */
		public String getName( ) {
			return name;
		}
		
		/** @return The value of this parameter. 
		 **/
		public T getValue( ) {			
			return value;	
		}
		
		/** @return The value of this parameter. 
		 **/
		public String getEncodedValue( ) {			
			return getEncodedValue(value.toString());	
		}
		
		private String getEncodedValue(String v) {
			try {
				return URLEncoder.encode(v, "UTF-8").replace("+", "%20");
			} catch(UnsupportedEncodingException e) {
				// should not happen
				LOGGER.error(e);
			}
			return "";	
		}
		
		@Override
		public String toString( ) {
			return name + "=" + value;
		}

		public void append(StringBuilder sb, String v) {
			sb.append(name);
			sb.append("=");
			sb.append(getEncodedValue(v));
		}
		
		public void append(StringBuilder sb) {
			
			if (value instanceof Collection<?> ) {
				for (Object o : (Collection<?>)value) {
					append(sb, o.toString());
					sb.append("&");
				}
				if (sb.length() > 0) {
					sb.setLength(sb.length() - 1);
				}
			} else if (value instanceof byte[]) {				
				append(sb, Base64.encode((byte[]) value));
			} else if (value instanceof Date) {
				synchronized (ISO_FORMAT) {
					append(sb, ISO_FORMAT.format(value));	
				}
							
			}else {
				append(sb, value.toString());			
			}
		}
		
	}
	
	/** The types of query parameter that can be used to construct the URL. */
	public static enum ParameterType {
		/** A URL query e.g. http://server/service/method?a=param1&b=param2 */
		QUERY,
		
		/** A path query e.g. http://server/service/method/param1/param2 */
		PATH;
	}
	
	/** The node logger instance. */
	private static final NodeLogger LOGGER = NodeLogger.getLogger(RESTWebServiceClient.class);
	
	/** Data for the POST content. */
	protected byte[] postContent;

	/** Flag to be used for non-DWS APIs for the POST content. */
	protected boolean useStringEntity;
	
	/** Whether to disable the code for creating/manipulating the URL. */
	protected boolean createURL;
	
	/** The type of query parameters to create. */
	private ParameterType parameterType = ParameterType.QUERY;	
		
	/** The credentials to use when making the calls using NTLM. */
	private NtlmPasswordAuthentication credentials;
	
	/** List of header suppliers. **/
	protected List<IErlwoodHeaderSupplier> headerSuppliers = new ArrayList<IErlwoodHeaderSupplier>();
	
	/** Create an instance of a REST Web Service Client.
	 * @param flowMap The flow variables to extract credentials from.
	 * @param provider The credentials provider to extract credentials from.
	 * @param settings The web service configuration.
	 * @return The REST web service client to use to make the queries.
	 * @throws WebServiceException If an error occurs creating the client.
	 * @deprecated Replace by ApacheRESTWebServiceClient constructor. */
	@Deprecated
	public static RESTWebServiceClient createClient(final Map<String, FlowVariable> flowMap, 
			final CredentialsProvider provider, final WebServiceSettings settings)
				throws WebServiceException
	{
		return createClient(null, flowMap, provider, settings);
	}
	
	/** Create an instance of a REST Web Service Client.
	 * @param flowMap The flow variables to extract credentials from.
	 * @param provider The credentials provider to extract credentials from.
	 * @param settings The web service configuration.
	 * @return The REST web service client to use to make the queries.
	 * @throws WebServiceException If an error occurs creating the client.
	 * @deprecated Replace by ApacheRESTWebServiceClient constructor. */
	@Deprecated
	public static RESTWebServiceClient createClient(IOTiming timingInterface, final Map<String, FlowVariable> flowMap, 
			final CredentialsProvider provider, final WebServiceSettings settings)
				throws WebServiceException
	{
		LOG.debug("Creating REST client");
		
		// extract the credentials (if set)
		NtlmPasswordAuthentication credentials = null;
		if(flowMap != null && provider != null && settings.isUseCredentials( )) {
			credentials = AuthenticationUtils.getCredentials(
					flowMap, 
					provider, 
					settings.getCredentialsName( )
			);
			
		}
		
		// create and return the client
		RESTWebServiceClient client = createClient(timingInterface, credentials, settings);
		client.addHeader("Accept-Encoding", "gzip");
		
		//flowMap.put("__REST_TIMING__", new FlowVariable("__REST_TIMING__", 20));
		return client;
	}
	
	
	/** Create an instance of a REST Web Service Client.
	 * @param credentials The credentials to use to authenticate with the web service.
	 * @param settings The web service configuration.
	 * @return The REST web service client to use to make the queries.
	 * @throws WebServiceException If an error occurs creating the client.
	 * @deprecated Replace by ApacheRESTWebServiceClient constructor. */
	@Deprecated
	static RESTWebServiceClient createClient(final NtlmPasswordAuthentication credentials, 
			final WebServiceSettings settings)
				throws WebServiceException
	{
		return createClient(null, credentials, settings);
	}
	
	/** Create an instance of a REST Web Service Client.
	 * @param credentials The credentials to use to authenticate with the web service.
	 * @param settings The web service configuration.
	 * @return The REST web service client to use to make the queries.
	 * @throws WebServiceException If an error occurs creating the client.
	 * @deprecated Replace by ApacheRESTWebServiceClient constructor. */
	@Deprecated
	static RESTWebServiceClient createClient(IOTiming timingInterface, final NtlmPasswordAuthentication credentials, 
			final WebServiceSettings settings)
				throws WebServiceException
	{
		// 	We always want to return the Apache client now,
		//	as it will now (since 4.5.3 Apache libs) handle SSO for
		//	NTLM and Kerberos, plus seperate credentials.
		LOG.debug("Creating Apache REST client");
		return new ApacheRESTWebServiceClient(timingInterface, credentials, settings, null);
		
	}
	
	protected RESTWebServiceClient(final IOTiming timingInterface, final NtlmPasswordAuthentication credentials,
			final WebServiceSettings settings) throws WebServiceException
	{
		super(timingInterface, settings);
		this.credentials = credentials;
		postContent = null;
		createURL = true;
        
        // 	Gets the Header Suppliers
		IExtensionRegistry reg = Platform.getExtensionRegistry();
    	for (IConfigurationElement element : reg.getConfigurationElementsFor("org.erlwood.knime.webservice.core.ErlwoodHeaderSupplier")) {	    		
    		IErlwoodHeaderSupplier hs = null;
    		try {
    			hs = (IErlwoodHeaderSupplier) element.createExecutableExtension("class");
    			headerSuppliers.add(hs);
    		} catch(CoreException ex) {
                // Do nothing
            	LOGGER.error(ex.getMessage(), ex);
            }
    	}
	}
	//required for testing
	protected RESTWebServiceClient() {
		super();
	}

	/** 
	 * @return The credentials
	 */
	public NtlmPasswordAuthentication getCredentials() {
		return credentials;
	}
	
	/** Set whether to create the URL including method and RESTParameter list or not.
	 * @param createURL Whether to create the URL (true) or not (false). */
	public void setCreateURL(final boolean createURL) {
		this.createURL = createURL;
	}

	/** Set the content type for the POST query.
	 * @param type The type of content for the POST. */
	public void setContentType(final String type) {
		replaceHeader("Content-Type", type);
	}
	
	/** Invoke the REST web service request using GET.
	 * @param exec The ExecutionContext used to check for cancellation.
	 * @param method The method on the web service to call.
	 * @param type The type of parameter URL to create.
	 * @param parameters The parameters to append to the URL.
	 * @return The response from the web service.
	 * @throws Exception */
	public final InputStream invokeGet(final ExecutionContext exec, final String method, 
			final ParameterType type, final Object...parameters) 
					throws Exception 
	{
		parameterType = type;
		return callWebService(exec, method, parameters);
	}
	
	/** Invoke the REST web service request using POST.
	 * @param exec The ExecutionContext used to check for cancellation.
	 * @param method The method on the web service to call.
	 * @param data The data content to include in the POST request.
	 * @param type The type of parameter URL to create.
	 * @param parameters The parameters to append to the URL.
	 * @return The response from the web service.
	 * @throws Exception */
	public final InputStream invokePost(final ExecutionContext exec, final String method, 
			final byte[] data, final ParameterType type, final Object... parameters)
				throws Exception
	{	
		parameterType = type;
		
		// assign POST data stream
		if(data != null) {
			//	If we are allowed to compress, do it here
			if (canCompress() && data.length > 1024) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				GZIPOutputStream gos = new GZIPOutputStream(baos);
				gos.write(data);
				gos.finish();
				gos.close();
				postContent = baos.toByteArray();
				baos.close();
				addHeader("Content-Encoding", "gzip");				
			} else {
				postContent = data;
			}
			
			// set the content type if it's not already set
			if(!headers.containsKey("Content-Type")) {
				setContentType("application/octet-stream");
			}
		}
		
		// return result
		return callWebService(exec, method, parameters);
	}
	
	
	/** Invoke the REST web service request using POST.
	 * @param exec The ExecutionContext used to check for cancellation.
	 * @param method The method on the web service to call.
	 * @param data The data content to include in the POST request.
	 * @param useStringEntity flag to notify the HTTP client to use JSON-type StringEntity.
	 * @param parameters The parameters to append to the URL.
	 * @return The response from the web service.
	 * @throws Exception */
	public final InputStream invokePost(final ExecutionContext exec, final String method, 
			final byte[] data, boolean useStringEntity, final Object... parameters) throws Exception {	
		
		this.useStringEntity = useStringEntity;
		postContent = data;
		
		// return result
		return callWebService(exec, method, parameters);
	}
	
	
	/** Parse the parameters and build the end of the URL containing the query.
	 * @param parameters The parameters to parse.
	 * @return The URL query created from the parameters. */
	protected String buildQueryURL(final Object[ ] parameters) {
		// if there are no parameters, return an empty query string
		if(parameters == null || parameters.length == 0) {
			return "";
		}
		
		// return the query string based on parameter type
		if(parameterType == ParameterType.QUERY) {
			return buildQueryString(parameters);
		} else {
			return buildPathString(parameters);
		}
	}
	
	/** Parse the parameters and build a query string from them.
	 * @param parameters The parameters to parse.
	 * @return The query string created from the parameters. */
	private String buildQueryString(final Object[ ] parameters) {		
		// iterate through the provided parameters to build the query string
		StringBuilder queryString = new StringBuilder( );
		boolean first = true;
		for(Object param : parameters) {
			if(param instanceof RESTParameter) {
				RESTParameter<?> p = (RESTParameter<?>)param;
				if(first) {
					queryString.append("?");
					first = false;
				} else {
					queryString.append("&");
				}
				p.append(queryString);
				
			}
		}
		
		return queryString.toString( );
	}
	
	/** Parse the parameters and build a path string from them.
	 * @param parameters The parameters to parse.
	 * @return The query string created from the parameters. */
	private String buildPathString(final Object[ ] parameters) {
		// iterate through the provided parameters to build the path string
		StringBuilder pathString = new StringBuilder( );
		for(Object param : parameters) {
			if(param instanceof String) {
				pathString.append("/");
				
				try {
					String value = URLEncoder.encode((String)param, "UTF-8");
					value = value.replace("+", "%20");
					pathString.append(value);
				} catch(UnsupportedEncodingException e) {
					// should not happen
					LOGGER.error(e);
				}
			}
		}
		
		return pathString.toString( );
	}
	
	/** Invoke the REST web service request.
	 * @param exec The ExecutionContext used to check for cancellation.
	 * @param method The method on the web service to call.
	 * @param parameters The query parameters.
	 * @return The response from the web service.
	 * @throws Exception */
	private InputStream callWebService(final ExecutionContext exec, final String method, 
			final Object... parameters) throws Exception 
	{
		// Process the header suppliers
		for (IErlwoodHeaderSupplier sup : headerSuppliers) {
			sup.setHeaders(this);
		}
		
		// return result
		Object[ ] result;
		if(exec != null) {
			result = invoke(exec, method, parameters);
		} else {
			result = invoke(method, parameters);
		}
		if(result == null || result.length == 0 || result[0] == null) {
			throw new WebServiceException("Request returned no result stream.");
		}
		return (InputStream)result[0];
	}

	/**
	 * Delegate InputStream used to extract the point that the stream is closed, which
	 * is assumed to be once it has been consumed.
	 * Used to for Web Service Call Timing.
	 * @author Luke Bullard
	 *
	 */
	protected class TimingInputStream extends InputStream {
		private final long startTime;
		private final InputStream _delegate;
		private boolean isClosed;
		protected TimingInputStream(InputStream is, long st) {
			_delegate = is;
			startTime = st;
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public int available() throws IOException {
			return _delegate.available();
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void close() throws IOException {
			if (!isClosed) {				
				addTiming(System.currentTimeMillis() - startTime);
				_delegate.close();
				isClosed = true;
			}
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(Object arg0) {
			return _delegate.equals(arg0);
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode() {
			return _delegate.hashCode();
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void mark(int arg0) {
			_delegate.mark(arg0);
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean markSupported() {
			return _delegate.markSupported();
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public int read() throws IOException {
			return _delegate.read();
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public int read(byte[] arg0, int arg1, int arg2) throws IOException {
			return _delegate.read(arg0, arg1, arg2);
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public int read(byte[] arg0) throws IOException {
			return _delegate.read(arg0);
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void reset() throws IOException {
			_delegate.reset();
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public long skip(long arg0) throws IOException {
			return _delegate.skip(arg0);
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString() {
			return _delegate.toString();
		}
	}	
	
}
