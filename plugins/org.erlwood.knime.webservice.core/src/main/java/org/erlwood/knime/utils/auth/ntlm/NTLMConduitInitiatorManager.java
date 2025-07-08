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

import jakarta.ws.rs.client.Invocation.Builder;

import org.apache.cxf.BusException;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.transport.ConduitInitiator;
import org.apache.cxf.transport.ConduitInitiatorManager;
import org.apache.cxf.transport.http.HTTPTransportFactory;
import org.apache.cxf.transport.http.asyncclient.AsyncHttpTransportFactory;

/**
 * Custom ConduitInitiatorManager that uses an asynchronous HTTP transport if NTLM authentication should be used (for
 * the current thread).
 *
 * @author Luke Bullard, Eli Lilly, UK
 */
class NTLMConduitInitiatorManager implements ConduitInitiatorManager {
	/**	The Singleton **/
	private static final NTLMConduitInitiatorManager INSTANCE = new NTLMConduitInitiatorManager();

	private final ThreadLocal<Boolean> isNtlm = new ThreadLocal<Boolean>();

	/**
	 * Returns the singleton instance for this class.
	 *
	 * @return the singleton instance
	 */
	public static NTLMConduitInitiatorManager getInstance() {
		return INSTANCE;
	}

	/**
	 * Private constructor for singleton pattern.
	 */
	private NTLMConduitInitiatorManager() {
		// Do nothing
	}

	/**
	 * Configures the given request for NTLM authentication
	 *
	 * @param request a request, must not be <code>null</code>
	 */
	void configureForNTLM(final Builder request) {
	    isNtlm.set(Boolean.TRUE);
	    try {
	        WebClient.getConfig(request).getHttpConduit();
	    } finally {
	        isNtlm.remove();
	    }
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void registerConduitInitiator(final String name, final ConduitInitiator factory) {
		// Do nothing
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deregisterConduitInitiator(final String name) {
		// Do nothing
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ConduitInitiator getConduitInitiator(final String name) throws BusException {
		return getConduitInitiatorForUri(null);
	}

    /**
     * Depending on the the value of isNtlm we will return either a standard {@link HTTPTransportFactory} or
     * {@link AsyncHttpTransportFactory}.
     */
	@Override
	public ConduitInitiator getConduitInitiatorForUri(final String uri) {
		Boolean b = isNtlm.get();
		if (b == Boolean.TRUE) {
			return new AsyncHttpTransportFactory();
		}
		return new HTTPTransportFactory();
	}
}
