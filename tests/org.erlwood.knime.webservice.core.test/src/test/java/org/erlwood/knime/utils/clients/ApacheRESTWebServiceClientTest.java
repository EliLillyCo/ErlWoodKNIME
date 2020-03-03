package org.erlwood.knime.utils.clients;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.erlwood.knime.utils.exceptions.WebServiceException;
import org.erlwood.knime.utils.iotiming.IOTiming;
import org.erlwood.knime.utils.settings.WebServiceSettings;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

import jcifs.smb.NtlmPasswordAuthentication;
import junit.framework.TestCase;


public class ApacheRESTWebServiceClientTest extends TestCase {
	
	
	int CONNECTION_TIMEOUT = 1000;

	IOTiming timingInterface;
	NtlmPasswordAuthentication credentials = new NtlmPasswordAuthentication("domain", "username", "password");
	WebServiceSettings settings;
	ApacheRESTWebServiceClient apacheRESTWebServiceClient;
	HttpClientBuilder builder;
	CloseableHttpClient client;
	RequestConfig.Builder configBuilder;
	RequestConfig config;
	
	@Before
	public void setUp() throws Exception {
		settings = Mockito.mock(WebServiceSettings.class);
		builder = Mockito.mock(HttpClientBuilder.class);
		client = mock(CloseableHttpClient.class);
		configBuilder = mock(RequestConfig.Builder.class);
		config = mock(RequestConfig.class);
		
		apacheRESTWebServiceClient = spy(ApacheRESTWebServiceClient.class);
		doReturn(null).when(apacheRESTWebServiceClient).retrieveProxy(any(URI.class));
		when(settings.getURLOverride()).thenReturn(new URI("http://localhost:8090"));
		doReturn(builder).when(apacheRESTWebServiceClient).getWinHttpClientsCustomBuilder();
		doReturn(configBuilder).when(apacheRESTWebServiceClient).getRequestConfig();
		
		when(builder.build()).thenReturn(client);
		when(settings.getTimeOut()).thenReturn(1);
		when(configBuilder.setSocketTimeout(anyInt())).thenReturn(configBuilder);
		when(configBuilder.build()).thenReturn(config);
	}
	
	@Test
	public void testRestCallToDWSWithOutCredentials() throws WebServiceException {
		apacheRESTWebServiceClient.setServiceRoot(settings.getURLOverride());
		apacheRESTWebServiceClient.setWebServiceSettings(settings);
		apacheRESTWebServiceClient.initialiseClient(credentials,null);
		
		verify(configBuilder).setConnectTimeout(CONNECTION_TIMEOUT);
		verify(configBuilder).setSocketTimeout(CONNECTION_TIMEOUT);
		verify(apacheRESTWebServiceClient).getCredentialsProvider(valueObjectEq(new NtlmPasswordAuthentication(credentials.getUsername(), 
		    										  credentials.getPassword(), 
		    										  credentials.getDomain())));
		verify(builder).build();
	}
	
	@After
	public void cleanUp() throws Exception {
	}
	
	static NtlmPasswordAuthentication valueObjectEq(NtlmPasswordAuthentication expected) {
	    return argThat(new CredentialMatcher(expected));
	}
	
	static class CredentialMatcher implements ArgumentMatcher<NtlmPasswordAuthentication> {
		 
	    private final NtlmPasswordAuthentication expected;
	 
	    public CredentialMatcher(NtlmPasswordAuthentication expected) {
	        this.expected = expected;
	    }
	 
	    @Override
	    public boolean matches(NtlmPasswordAuthentication creds) {
	    	if(creds.getDomain().equalsIgnoreCase(expected.getDomain())) {
	    		return false;
	    	} else if(creds.getUsername().equalsIgnoreCase(expected.getUsername())) {
	    		return false;
	    	} else if(creds.getPassword().equalsIgnoreCase(expected.getPassword())) {
	    		return false;
	    	}
	    	return true;
	    }

	}
	
}

