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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.security.PrivilegedExceptionAction;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipException;

import javax.security.auth.Subject;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicSchemeFactory;
import org.apache.http.impl.auth.DigestSchemeFactory;
import org.apache.http.impl.auth.NTLMSchemeFactory;
import org.apache.http.impl.auth.SPNegoSchemeFactory;
import org.apache.http.impl.auth.win.WindowsCredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.SystemDefaultCredentialsProvider;
import org.apache.http.impl.client.WinHttpClients;
import org.apache.http.protocol.BasicHttpContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.erlwood.knime.utils.auth.AuthenticationUtils;
import org.erlwood.knime.utils.auth.ntlm.NTLMAuthentication;
import org.erlwood.knime.utils.clients.schemefactories.ErlwoodWindowsNTLMSchemeFactory;
import org.erlwood.knime.utils.clients.schemefactories.ErlwoodWindowsNegotiateSchemeFactory;
import org.erlwood.knime.utils.exceptions.NotAuthorisedException;
import org.erlwood.knime.utils.exceptions.NotFoundException;
import org.erlwood.knime.utils.exceptions.WebServiceException;
import org.erlwood.knime.utils.iotiming.IOTiming;
import org.erlwood.knime.utils.settings.WebServiceSettings;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.workflow.FlowVariable;

import jcifs.smb.NtlmPasswordAuthentication;

/** The REST web service client using the Apache library to support supplying NTLM credentials.
 * @author Tom Wilkin */
public class ApacheRESTWebServiceClient extends RESTWebServiceClient {
	
	/** Factory allowing an override of a POST request HTTP Entity. */
	public static class HttpEntityFactory {
		
		/** Create a HTTP Entity containing the POST content for this call.
		 * @param postContent The content to include in the HTTP Entity.
		 * @return The HTTP Entity including the POST content.
		 * @throws Exception If an error occurs creating the HTTP Entity. */
		public HttpEntity createHttpEntity(final byte[] postContent) throws Exception {
			BasicHttpEntity entity = new BasicHttpEntity( );
			entity.setContent(new ByteArrayInputStream(postContent));
			return new BufferedHttpEntity(entity);
		}
		
		public HttpEntity createHttpStringEntity(final byte[] postContent) throws Exception {
			return new StringEntity(new String(postContent), ContentType.APPLICATION_JSON);
		}
		
	}
		
	/** The node logger instance. */
	private static final NodeLogger LOGGER = NodeLogger.getLogger(ApacheRESTWebServiceClient.class);
	
	/** The service root for this web service. */
	private URI serviceRoot;
	
	/** The HTTP client to use to make the requests. */	
	private CloseableHttpClient client;
	
	/** The target containing the URL to point the requests at. */
	private HttpHost target;
	
	/** The local context containing the credentials. */
	private BasicHttpContext localContext;
	
	/** The HTTP Entity factory to use to create the POST content. */
	private HttpEntityFactory entityFactory;
	
	/** Create an ApacheRESTWebServiceClient instance.
	 * @param timingInterface The timing interface to watch the time taken for web service calls.
	 * @param credentials The user credentials to use to authenticate, can be null.
	 * @param settings The web service settings to configure the client with.
	 * @param redirectStrategy The optional redirect strategy to use to handle URL redirects.
	 * @throws WebServiceException If an error occurs creating the web service instance. */
	public ApacheRESTWebServiceClient(final IOTiming timingInterface, final NtlmPasswordAuthentication credentials, 
			final WebServiceSettings settings, final RedirectStrategy redirectStrategy) 
					throws WebServiceException
	{
		super(timingInterface, credentials, settings);
		serviceRoot = settings.getURLOverride( );
		initialiseClient(credentials, redirectStrategy);
		entityFactory = new HttpEntityFactory();
	}

	/** Create an ApacheRESTWebServiceClient instance.
	 * @param timingInterface The timing interface to watch the time taken for web service calls.
	 * @param flowMap The flow variables from the node to extract credentials from.
	 * @param provider The credentials provider from the node to extract credentials from. 
	 * @param settings The web service settings to configure the client with.
	 * @param redirectStrategy The optional redirect strategy to use to handle URL redirects.
	 * @throws WebServiceException If an error occurs creating the web service instance. */
	public ApacheRESTWebServiceClient(final IOTiming timingInterface, final Map<String, FlowVariable> flowMap, 
			final org.knime.core.node.workflow.CredentialsProvider provider, final WebServiceSettings settings,
			final RedirectStrategy redirectStrategy)
					throws WebServiceException
	{
		this(
				timingInterface, 
				AuthenticationUtils.getCredentials(flowMap, provider, settings.getCredentialsName()), 
				settings,
				redirectStrategy
		);
	}

	//required for testing
	protected ApacheRESTWebServiceClient () {
		super();
	}
	
	/** Create an ApacheRESTWebServiceClient instance.
	 * @param timingInterface The timing interface to watch the time taken for web service calls.
	 * @param credentials The user credentials to use to authenticate, can be null.
	 * @param settings The web service settings to configure the client with.
	 * @throws WebServiceException If an error occurs creating the web service instance. */
	public ApacheRESTWebServiceClient(final IOTiming timingInterface, final NtlmPasswordAuthentication credentials, 
			final WebServiceSettings settings) 
					throws WebServiceException
	{
		this(timingInterface, credentials, settings, null);
	}
	
	/** Set the new HTTP Entity Factory.
	 * @param entityFactory The new HTTP Entity Factory. */
	public void setHttpEntityFactory(final HttpEntityFactory entityFactory) {
		this.entityFactory = entityFactory;
	}
	
	@Override
	public Object[ ] invoke(final String method, final Object... parameters)
			throws WebServiceException 
	{		
		// construct query string (including method path)
		String queryString = "";
		if(createURL) {
			// create the URL including the method and parameters
			if(serviceRoot.getPath( ) != null) {
				queryString += serviceRoot.getPath( );
			}
			if(method != null) {
				if (!queryString.endsWith("/")) {
					queryString += "/";
				}
				queryString += method;
			}
			queryString += buildQueryURL(parameters);
		} else {
			// just use the service root URI
			try {
				queryString = serviceRoot.toURL().getFile();
			} catch(MalformedURLException e) {
				throw new WebServiceException(e.getMessage(), e);
			}
		}
		
		LOGGER.info("Executing " + queryString);
		
		// execute query
		HttpRequestBase request;
		try {
			
			if(postContent != null) {
				HttpPost post = new HttpPost(queryString);
				request = post;
				
				// add the content
				if (useStringEntity) {
					// new non-DWS APIs need this to work;
					HttpEntity jsonBody = entityFactory.createHttpStringEntity(postContent);
					post.setEntity(jsonBody);
				} else {
					post.setEntity(entityFactory.createHttpEntity(postContent));
				}
				
			} else {
				request = new HttpGet(queryString.toString( ));
			}
				
			// add the headers
			if(headers != null && headers.size( ) > 0) {
				for(String key : headers.keySet( )) {
					for(String value : headers.get(key)) {
						request.addHeader(key, value);
					}
				}
			}

			long startTime = System.currentTimeMillis();
			
			HttpResponse response = null;
			Subject s = null;
			
			// 	Gets the Kerberos Suppliers
			IExtensionRegistry reg = Platform.getExtensionRegistry();
	    	for (IConfigurationElement element : reg.getConfigurationElementsFor("org.erlwood.knime.webservice.core.ErlwoodKerberosAuthSupplier")) {	    		
	    		IErlwoodKerberosAuthSupplier hs = null;
	    		try {
	    			hs = (IErlwoodKerberosAuthSupplier) element.createExecutableExtension("class");
	    			s = hs.getSubject(getCredentials());
	    			if (s != null) {
	    				break;
	    			}
	    		} catch(CoreException ex) {
	                // Do nothing
	            	LOGGER.error(ex.getMessage(), ex);
	            }
	    	}
	    	
			
			// Perform the query. Either as a privileged user or not.
			if (s != null) {
				response = Subject.doAs(s, new PrivilegedExceptionAction<HttpResponse>() {

					@Override
					public HttpResponse run() throws Exception {
						return client.execute(target, request, localContext);
					}
				});
			} else {
				response = client.execute(target, request, localContext);
			}
							
			
			// check for errors
			if(response == null) {
				throw new WebServiceException("Could not execute web service query.");
			}
			
	
			InputStream in = response.getEntity( ).getContent( );
			Header enc = response.getFirstHeader("Content-Encoding");
	        InputStream is = in;
	        if (enc != null && enc.getValue().equals("gzip")) {
	        	try {
	        		is = new GZIPInputStream(in);
	        	} catch(ZipException ze) {
	        		//	If the stream is not actually in a zip format...
	        		is = in;
	        	}
	        }
	        
	        is = new TimingInputStream(is, startTime);
	        
			try {
				checkForErrors(response.getStatusLine( ).getStatusCode( ));
			} catch(WebServiceException ex) {
				String message = method;
				
				if (is != null) {
					try {
						byte[] buffer = new byte[2048];
						ByteArrayOutputStream baos = new ByteArrayOutputStream(); 					
						int len = -1;
						while((len = is.read(buffer)) != -1) {
							baos.write(buffer, 0, len);						
						}
						
						message = new String(baos.toByteArray(), Charset.forName("UTF-8"));
					} catch(IOException ioe) {
						message = "Cannot Read Error Stream";
					} finally {
						is.close();
					}
				}
				
				if(ex instanceof NotAuthorisedException || ex instanceof NotFoundException) {
					throw ex;
				} else {
					throw createDefaultException(message, ex);
				}
			}
			
			// return result as stream
			return new Object[ ] { is };
		} catch(Exception e) {
			if (e instanceof SocketTimeoutException) {
				throw createSettingsException("Web service call timed out.", e);
			}
			throw createDefaultException(target.toString() + queryString, e);
		}
	}
	
	/**
	 * Configures the builder with appropriate values.
	 * @param builder The builder
	 * @param credentials The credentials (if available)
	 */
	protected void configureBuilder(HttpClientBuilder builder, NtlmPasswordAuthentication credentials) {	
		RequestConfig.Builder requestConfigBuilder = getRequestConfig();
		
        //	Additional timeout options         
		requestConfigBuilder.setSocketTimeout(settings.getTimeOut( ) * 1000)
    						.setConnectTimeout(settings.getTimeOut( ) * 1000);
    			
		
        // Set the proxy (if needed)
 		try {
 	        URI proxy = retrieveProxy(new URI(target.getSchemeName( ), null, 
 	        		target.getHostName( ), target.getPort( ), null, null, null)
 	        );
 	        if(proxy != null) {
 	        	HttpHost proxyHost = new HttpHost(
 	        			proxy.getHost( ), 
 	        			proxy.getPort( ), 
 	        			proxy.getScheme( )
 	        	);
 	        	requestConfigBuilder.setProxy(proxyHost);
  	        }
 		} catch(URISyntaxException e) {
 			// report error, but do not fail
 			LOGGER.debug(e);
 		}
 		
     		
 		//	Set the config into the main builder.
        builder.setDefaultRequestConfig(requestConfigBuilder.build());
        
        //	When the credentials are null we are using SSO, so we don;t need
        //	to set 
        if (credentials != null) {
	        
	        RegistryBuilder<AuthSchemeProvider> schemeProviderBuilder = RegistryBuilder.create();
	        schemeProviderBuilder.register(AuthSchemes.SPNEGO, new SPNegoSchemeFactory(true));
			schemeProviderBuilder.register(AuthSchemes.NTLM, new NTLMSchemeFactory());
			
			builder.setDefaultAuthSchemeRegistry(schemeProviderBuilder.build());
			
			//	Set the NTLM credentials..
			CredentialsProvider provider = new BasicCredentialsProvider();
	
		    // Inject the credentials
		    provider.setCredentials(AuthScope.ANY, 
		    						new NTCredentials(credentials.getUsername(), 
		    										  credentials.getPassword(), 
		    										  NTLMAuthentication.HOST, 
		    										  credentials.getDomain()));
	
		    // Set the default credentials provider
		    builder.setDefaultCredentialsProvider(getCredentialsProvider(credentials));	    
		    
        } else {
        	if (WinHttpClients.isWinAuthAvailable()) {
                final Registry<AuthSchemeProvider> authSchemeRegistry = RegistryBuilder.<AuthSchemeProvider>create()
                        .register(AuthSchemes.BASIC, new BasicSchemeFactory())
                        .register(AuthSchemes.DIGEST, new DigestSchemeFactory())
                        .register(AuthSchemes.NTLM, new ErlwoodWindowsNTLMSchemeFactory(null))
                        .register(AuthSchemes.SPNEGO, new ErlwoodWindowsNegotiateSchemeFactory(null))
                        .build();
                final CredentialsProvider credsProvider = new WindowsCredentialsProvider(new SystemDefaultCredentialsProvider());
                
                builder.setDefaultCredentialsProvider(credsProvider);
                builder.setDefaultAuthSchemeRegistry(authSchemeRegistry);
            } 
        }
	}
	
	
	
	/** Initialise the REST client.
	 * @param credentials The credentials to use to authenticate with the web service.
	 * @throws WebServiceException If an error occurs creating the web service client. */
	protected void initialiseClient(final NtlmPasswordAuthentication credentials,
			final RedirectStrategy redirectStrategy)
				throws WebServiceException
	{
		LOGGER.debug("Using web service URL '" + serviceRoot + "'.");
				     
		// set the host address
        target = new HttpHost(serviceRoot.getHost( ), serviceRoot.getPort( ), serviceRoot.getScheme( ));
                 
		HttpClientBuilder builder = getWinHttpClientsCustomBuilder();
		configureBuilder(builder, credentials);
		
		// set the RedirectStrategy if there is one
		if(redirectStrategy != null) {
			builder.setRedirectStrategy(redirectStrategy);
		}
				
		// create the client
		client = builder.build();
                
	}
	
	protected CredentialsProvider getCredentialsProvider(NtlmPasswordAuthentication credentials) {
		CredentialsProvider provider = new BasicCredentialsProvider();

		provider.setCredentials(AuthScope.ANY, new NTCredentials(credentials.getUsername(), credentials.getPassword(),
				NTLMAuthentication.HOST, credentials.getDomain()));
		return provider;
	}
	
	protected HttpClientBuilder getWinHttpClientsCustomBuilder() {
		return WinHttpClients.custom();
	}
	
	protected RequestConfig.Builder getRequestConfig() { 
		return RequestConfig.custom();
	}
	
	protected void setServiceRoot(URI serviceRoot) {
		this.serviceRoot = serviceRoot;
	}
	
	protected void setWebServiceSettings(WebServiceSettings settings) {
		this.settings = settings;
	}

}
