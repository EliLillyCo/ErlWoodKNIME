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
package org.erlwood.knime.utils.auth.ntlm;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.client.Invocation.Builder;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.jaxrs.client.ClientConfiguration;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.transport.ConduitInitiatorManager;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.knime.core.data.DataRow;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.workflow.CredentialsProvider;
import org.knime.core.node.workflow.FlowVariable;
import org.knime.core.node.workflow.ICredentials;
import org.knime.rest.generic.UsernamePasswordAuthentication;
/**
 * NTLM Authentication.
 *
 * @author Luke Bullard, Eli Lilly, UK
 */
public class NTLMAuthentication extends UsernamePasswordAuthentication {
	/** The logger. **/
	private static final NodeLogger LOG = NodeLogger.getLogger(NTLMAuthentication.class);
	
	/** The host name of this machine. **/
    public static final String HOST;
    
    /** The list of available header suppliers. **/
    private static final List<IHeaderSupplier> HEADER_SUPPLIERS = new ArrayList<IHeaderSupplier>();
    
    static {
        String hostName = "host";
        try {
            hostName = InetAddress.getLocalHost().getCanonicalHostName();
        } catch (Exception ex) {
            // Do nothing
        	LOG.error(ex.getMessage(), ex);
        }
        HOST = hostName;
        
        IExtensionRegistry reg = Platform.getExtensionRegistry();
        
        // 	Gets the Header Suppliers
        try {
	    	for (IConfigurationElement element : reg.getConfigurationElementsFor("org.erlwood.knime.core.KnimeRestHeaderSupplier")) {	    		
	    		IHeaderSupplier hs = (IHeaderSupplier) element.createExecutableExtension("class");
	    		HEADER_SUPPLIERS.add(hs);    		
	    	}
        } catch(Exception ex) {
            // Do nothing
        	LOG.error(ex.getMessage(), ex);
        }    
    }

    /**
     * Constructs with the empty defaults. (This constructor is called for the automatic instantiation.)
     */
    public NTLMAuthentication() {
        super("NTLM auth", "", "", "");
        
        //	Set the custom conduit initiator.
        Bus bus = BusFactory.getThreadDefaultBus();
        bus.setExtension(NTLMConduitInitiatorManager.getInstance(), ConduitInitiatorManager.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Builder updateRequest(final Builder request, final DataRow row, final CredentialsProvider credProvider,
        final Map<String, FlowVariable> flowVariables) {
        NTLMConduitInitiatorManager.getInstance().configureForNTLM(request);
        ClientConfiguration conf = WebClient.getConfig(request);
     
        //	Set any custom headers       
		for (IHeaderSupplier hs : HEADER_SUPPLIERS) {
	        for (Entry<String, Object> es : hs.getHeaders().entrySet()) {
				request.header(es.getKey(), es.getValue());		
			}
        }                    
        
        conf.getHttpConduit().setAuthorization(null);
        conf.getRequestContext().put("use.async.http.conduit", Boolean.TRUE);
        conf.getRequestContext().put(Credentials.class.getName(), getNTCredentials(credProvider));

        HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
        httpClientPolicy.setAllowChunking(false);
        httpClientPolicy.setAutoRedirect(true);
        httpClientPolicy.setMaxRetransmits(1);

        conf.getHttpConduit().setClient(httpClientPolicy);


        return request;
    }

    /**
     * Gets a Credentials object populated with either the values from the {@link ICredentials} object or the typed in
     * username and password fields.
     *
     * @param credProvider the {@link org.apache.http.client.CredentialsProvider} to use
     * @return a credentials object to be used in the NTLM call
     */
    private Credentials getNTCredentials(final CredentialsProvider credProvider) {
        if (credProvider == null) {
            throw new IllegalArgumentException("No credentials provider provided");
        }

        String credentialsName = getCredential();
        String username = getUsername();
        String password = getPassword();

        if (!StringUtils.isEmpty(credentialsName)) {
            try {
                ICredentials cred = credProvider.get(credentialsName);
                username = cred.getLogin();
                password = cred.getPassword();
            } catch (IllegalArgumentException ex) {
                throw new IllegalStateException("Missing credentials for " + credentialsName);
            }
        }
        
        //	Check username & password
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
        	throw new IllegalStateException("Username or Password cannot be blank !");
        }
        return new NTCredentials(username, password, HOST, "");
    }
}
