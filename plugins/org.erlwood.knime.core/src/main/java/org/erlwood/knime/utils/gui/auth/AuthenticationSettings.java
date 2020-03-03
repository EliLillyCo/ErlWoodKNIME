/*
 * ------------------------------------------------------------------------
 *
 * Copyright (C) 2015 Eli Lilly and Company Limited
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
package org.erlwood.knime.utils.gui.auth;

import org.erlwood.knime.utils.auth.AuthenticationUtils;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

/** Base settings class to store the selection of workflow credentials used by multiple nodes.
 * @author Tom Wilkin */
public abstract class AuthenticationSettings {
	
	/** The name of the credentials name property in the settings. */
	private static final String CREDENTIALS_NAME_SETTINGS = "credentialsName";
	
	/** The name of the use credentials property in the settings. */
	private static final String USE_CREDENTIALS_SETTINGS = "useCredentials";
	
	/** The value for a string to store when it should be the default from the preferences. */
	protected static final String DEFAULT_STR = "#default#";
	
	/** The value of the credentials name property. */
	protected String credentialsName;
	
	/** Whether to use credentials or not. */
	protected boolean useCredentials;
	
	protected AuthenticationSettings(final boolean useCredentials) {
		credentialsName = AuthenticationUtils.getDefaultCredentialsName( );
		this.useCredentials = useCredentials;
	}
	
	protected AuthenticationSettings(final AuthenticationSettings other) {
		if(other != null) {
			credentialsName = other.credentialsName;
			useCredentials = other.useCredentials;
		}
	}
	
	protected AuthenticationSettings( ) {
		this(true);
	}
	
	/** @return The user specified credentials object to use. */
	public String getCredentialsName( ) {
		if(useCredentials && credentialsName == null) {
			// use the default if we have it selected to use credentials and none are selected
			return AuthenticationUtils.getDefaultCredentialsName( );
		}
		
		if(DEFAULT_STR.equals(credentialsName)) {
			return AuthenticationUtils.getDefaultCredentialsName( );
		} else {
			return credentialsName;
		}
	}
	
	/** @return Whether the credentials selection is enabled/disabled. */
	public boolean isUseCredentials( ) {
		return useCredentials;
	}
	
	/** Set the value of the user specified credentials value.
	 * @param credentialsName The name of the credentials to use. */
	public void setCredentialsName(final String credentialsName) {
		if(AuthenticationUtils.getDefaultCredentialsName( ).equals(credentialsName)) {
			this.credentialsName = DEFAULT_STR;
		} else {
			this.credentialsName = credentialsName;
		}
	}
	
	/** Set whether to use credentials or not.
	 * @param useCredentials Whether to use credentials or not. */
	public void setUseCredentials(final boolean useCredentials) {
		this.useCredentials = useCredentials;
	}
	
	/** Save the settings of this dialog tab.
	 * @param settings The settings to store the configuration in. */
	public void saveSettingsTo(final NodeSettingsWO settings) {
		settings.addString(CREDENTIALS_NAME_SETTINGS, credentialsName);
		settings.addBoolean(USE_CREDENTIALS_SETTINGS, useCredentials);
	}
	
	/** Load the settings to this dialog tab.
	 * @param settings The settings to load the configuration from. */
	public void loadSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		credentialsName = settings.getString(CREDENTIALS_NAME_SETTINGS, DEFAULT_STR);
		useCredentials = settings.getBoolean(USE_CREDENTIALS_SETTINGS, true);
	}

	/** Validate the settings.
	 * @param settings The Node configuration containing the settings.
	 * @throws InvalidSettingsException If there is an issue with the value of the settings. */
	public void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		// by default does nothing
	}

}
