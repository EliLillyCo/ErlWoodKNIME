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
package org.erlwood.knime.utils.auth;

import java.util.Map;
import java.util.regex.Pattern;

import org.erlwood.knime.CoreActivator;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.workflow.CredentialsProvider;
import org.knime.core.node.workflow.FlowVariable;
import org.knime.core.node.workflow.ICredentials;

import com.hierynomus.smbj.auth.AuthenticationContext;

import jcifs.smb.NtlmPasswordAuthentication;

public final class AuthenticationUtils {
	private static final NodeLogger LOG = NodeLogger.getLogger(AuthenticationUtils.class);

	private AuthenticationUtils() {

	}
	
	public static AuthenticationContext getAuthenticationContext(final Map<String, FlowVariable> flowMap, 
			final CredentialsProvider provider, final String credentialsName) {		
		ICredentials cred = null;

		String credUserName = "";
		String credPassword = "";
		String domain = "";

		// Test for system properties
		if (isFlowVariableCredentialsSet(flowMap)) {
			LOG.debug("Using flow variables for credentials");

			credUserName = flowMap.get(CoreActivator.getString(CoreActivator.NT_USERNAME)).getStringValue();
			credPassword = flowMap.get(CoreActivator.getString(CoreActivator.NT_PASSWORD)).getStringValue();

			if (credUserName == null || credPassword == null) {
				throw new IllegalArgumentException(
						CoreActivator.getString(CoreActivator.NT_CREDS) + " is set, but one of " + 
						CoreActivator.getString(CoreActivator.NT_USERNAME) + " or " + 
						CoreActivator.getString(CoreActivator.NT_PASSWORD) + " is not.");
			}

		} else {
			
			if(credentialsName == null) {
				LOG.debug("credentialsName is NULL");
				return null;
			}

			try {
				cred = provider.get(credentialsName);
				LOG.debug("Found ICredentials for " + cred.getLogin());
				credUserName = cred.getLogin();
				credPassword = cred.getPassword();
				if(credUserName == null || "".equals(credUserName)) {
					LOG.debug("Missing username for " + getDefaultCredentialsName( ));
					return null;
				}
				if(credPassword == null || "".equals(credPassword)) {
					LOG.debug("Missing password for " + getDefaultCredentialsName( ));
					return null;
				}
				if (credUserName.contains("\\")) {
					String[] splitcreds = credUserName.split(Pattern.quote("\\"));
					domain = splitcreds[0];
					credUserName = splitcreds[1];
				}

			} catch (IllegalArgumentException ex) {
				LOG.debug("Missing ICredentials for " + getDefaultCredentialsName( ));
				return null;
			} catch (NullPointerException e) {
				LOG.debug("Null ICredentials for " + getDefaultCredentialsName( ));
				return null;
			}
		}

		return new AuthenticationContext(credUserName, credPassword.toCharArray(), domain);

	}

	/**
	 * Extracts the Credentials from the workflow credentials provider passed
	 * in. If no credentials exist, a null is returned.
	 * 
	 * @param flowMap The flow variables for the node which is used to extract the credentials from.
	 * @param provider
	 *            The credentials provider to use
	 * @return a valid Credentials object or null
	 */
	public static NtlmPasswordAuthentication getCredentials(Map<String, FlowVariable> flowMap,
			CredentialsProvider provider) {
		return getCredentials(flowMap, provider, getDefaultCredentialsName( ));
	}
	
	/**
	 * Extracts the Credentials from the workflow credentials provider passed
	 * in. If no credentials exist, a null is returned.
	 * 
	 * @param flowMap The flow variables for the node which is used to extract the credentials from.
	 * @param provider
	 *            The credentials provider to use
	 * @param credentialsName The name of the credentials object to extract.
	 * @return a valid Credentials object or null
	 */
	public static NtlmPasswordAuthentication getCredentials(final Map<String, FlowVariable> flowMap, 
			final CredentialsProvider provider, final String credentialsName) 
	{
		if (flowMap == null || provider == null) {
			return null;
		}
		return  getNTLMAuth(flowMap, provider, credentialsName);
		
	}

	public static NtlmPasswordAuthentication getCredentials(NodeModel nm,
			final CredentialsProvider provider) {
		return getCredentials(nm.getAvailableFlowVariables(), provider);
	}

	public static NtlmPasswordAuthentication getCredentials(final NodeDialogPane ndp,
			final CredentialsProvider provider) {
		return getCredentials(ndp.getAvailableFlowVariables(), provider);
	}

	/**
	 * Extracts the NTLM credentials from the workflow credentials provider
	 * passed in. If no credentials exist, a null is returned.
	 * 
	 * @param provider
	 *            The credentials provider to use
	 * @return a valid NtlmPasswordAuthentication object or null
	 */
	public static NtlmPasswordAuthentication getNTLMAuth(NodeModel nm,
			CredentialsProvider provider) {
		if (nm == null || provider == null) {
			return null;
		}
		return getNTLMAuth(nm.getAvailableFlowVariables(), provider);
	}
	
	/**
	 * Extracts the NTLM credentials from the workflow credentials provider
	 * passed in. If no credentials exist, a null is returned.
	 * 
	 * @param nm The NodeModel to extract the flow variables from.
	 * @param provider
	 *            The credentials provider to use
	 * @param credentialsName The name of the credentials object to extract.
	 * @return a valid NtlmPasswordAuthentication object or null
	 */
	public static NtlmPasswordAuthentication getNTLMAuth(final NodeModel nm,
			final CredentialsProvider provider, final String credentialsName) {
		if (nm == null || provider == null) {
			return null;
		}
		return getNTLMAuth(nm.getAvailableFlowVariables(), provider, credentialsName);
	}

	/**
	 * Extracts the NTLM credentials from the workflow credentials provider
	 * passed in. If no credentials exist, a null is returned.
	 * 
	 * @param ndp The NodeDialogPane to extract the flow variables from.
	 * @param provider
	 *            The credentials provider to use.
	 * @return a valid NtlmPasswordAuthentication object or null.
	 */
	public static NtlmPasswordAuthentication getNTLMAuth(
			final NodeDialogPane ndp, final CredentialsProvider provider) {
		if (ndp == null || provider == null) {
			return null;
		}
		return getNTLMAuth(ndp.getAvailableFlowVariables(), provider);
	}
	
	/**
	 * Extracts the NTLM credentials from the workflow credentials provider
	 * passed in. If no credentials exist, a null is returned.
	 * 
	 * @param ndp The NodeDialogPane to extract the flow variables from.
	 * @param provider
	 *            The credentials provider to use.
	 * @param credentialsName The name of the credentials object to extract.
	 * @return a valid NtlmPasswordAuthentication object or null.
	 */
	public static NtlmPasswordAuthentication getNTLMAuth(final NodeDialogPane ndp, 
			final CredentialsProvider provider, final String credentialsName) 
	{
		if (ndp == null || provider == null) {
			return null;
		}
		return getNTLMAuth(ndp.getAvailableFlowVariables(), provider, credentialsName);
	}

	/**
	 * Extracts the NTLM credentials from the workflow credentials provider
	 * passed in. If no credentials exist, a null is returned.
	 * 
	 * @param flowMap The flow variables for the node which is used to extract the credentials from.
	 * @param provider
	 *            The credentials provider to use
	 * @return a valid NtlmPasswordAuthentication object or null
	 */
	public static NtlmPasswordAuthentication getNTLMAuth(
			Map<String, FlowVariable> flowMap, CredentialsProvider provider) 
	{
		return getNTLMAuth(flowMap, provider, getDefaultCredentialsName( ));
	}
	
	/**
	 * Extracts the NTLM credentials from the workflow credentials provider
	 * passed in. If no credentials exist, a null is returned.
	 * 
	 * @param flowMap The flow variables for the node which is used to extract the credentials from.
	 * @param provider
	 *            The credentials provider to use
	 * @param credentialsName The name of the credentials object to extract.
	 * @return a valid NtlmPasswordAuthentication object or null
	 */
	public static NtlmPasswordAuthentication getNTLMAuth(final Map<String, FlowVariable> flowMap, 
			final CredentialsProvider provider, final String credentialsName) 
	{		
		ICredentials cred = null;

		String credUserName = "";
		String credPassword = "";

		// Test for system properties
		if (isFlowVariableCredentialsSet(flowMap)) {
			LOG.debug("Using flow variables for credentials");

			credUserName = flowMap.get(CoreActivator.getString(CoreActivator.NT_USERNAME)).getStringValue();
			credPassword = flowMap.get(CoreActivator.getString(CoreActivator.NT_PASSWORD)).getStringValue();

			if (credUserName == null || credPassword == null) {
				throw new IllegalArgumentException(
						CoreActivator.getString(CoreActivator.NT_CREDS) + " is set, but one of " + 
						CoreActivator.getString(CoreActivator.NT_USERNAME) + " or " + 
						CoreActivator.getString(CoreActivator.NT_PASSWORD) + " is not.");
			}

		} else {
			
			if(credentialsName == null) {
				LOG.debug("credentialsName is NULL");
				return null;
			}

			try {
				cred = provider.get(credentialsName);
				LOG.debug("Found ICredentials for " + cred.getLogin());
				credUserName = cred.getLogin();
				credPassword = cred.getPassword();
				if(credUserName == null || "".equals(credUserName)) {
					LOG.debug("Missing username for " + getDefaultCredentialsName( ));
					return null;
				}
				if(credPassword == null || "".equals(credPassword)) {
					LOG.debug("Missing password for " + getDefaultCredentialsName( ));
					return null;
				}
			} catch (IllegalArgumentException ex) {
				LOG.debug("Missing ICredentials for " + getDefaultCredentialsName( ));
				return null;
			} catch (NullPointerException e) {
				LOG.debug("Null ICredentials for " + getDefaultCredentialsName( ));
				return null;
			}
		}

		//int iPos = credUserName.indexOf('\\');
		/*if (iPos == -1) {
			throw new IllegalArgumentException(
					"Username MUST be in the form domain\\username");
		}*/
		//String domain = credUserName.substring(0, iPos);
		//String username = credUserName.substring(iPos + 1);
		return new NtlmPasswordAuthentication("", credUserName, credPassword);

	}
	
	/** Retrieve the default credentials name from the preferences.
	 * @return The default credentials name from the preferences. */
	public static String getDefaultCredentialsName( ) {
		return CoreActivator.getString(CoreActivator.WORKFLOW_CREDENTIALS);
	}
	
	/** Check if there are credentials set via flow variables.
	 * @param flowMap The flow variable to check for credentials.
	 * @return Whether credentials have been set by a flow variable. */
	public static boolean isFlowVariableCredentialsSet(final Map<String, FlowVariable> flowMap) {
		return flowMap != null && flowMap.containsKey(CoreActivator.getString(CoreActivator.NT_CREDS));
	}
	
	/** Check if some workflow credentials have been set.
	 * @throws InvalidSettingsException If no workflow credentials have been set. */
	public static void checkRequiredCredentials(final Map<String, FlowVariable> flowMap, 
			final CredentialsProvider provider, final String credentialsName) 
					throws InvalidSettingsException 
	{
		NtlmPasswordAuthentication creds = getCredentials(flowMap, provider, credentialsName);
		if(creds == null) {
			throw new InvalidSettingsException(
					"This node requires that workflow credentials are set, but none have been set."
			);
		}
	}
	
}
