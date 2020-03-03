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
package org.erlwood.knime.utils.settings;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.erlwood.knime.utils.gui.auth.AuthenticationSettings;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import org.erlwood.knime.utils.IWebServicePreferenceSupplier;
import org.erlwood.knime.utils.WebServicePreferenceURLRetriever;
import org.erlwood.knime.utils.clients.WebServiceClient;
import org.erlwood.knime.utils.exceptions.WebServiceException;

/** Settings for the additional properties added for web service nodes.
 * @author Tom Wilkin */
public class WebServiceSettings extends AuthenticationSettings {
	
	/** The logger instance. */
	private static final NodeLogger LOG = NodeLogger.getLogger(WebServiceSettings.class);
	
	/** The name of the time out property in the settings. */
	private static final String TIME_OUT_ID = "timeOut";
	
	/** The name of the max child elements property in the settings. */
	private static final String MAX_CHILD_ELEMENTS_ID = "maxChildElements";
	
	/** The name of the use URL override property in the settings. */
	private static final String USE_URL_OVERRIDE_ID = "useUrlOverride";
	
	/** The name of the URL override property in the settings. */
	private static final String URL_OVERRIDE_ID = "urlOverride";
		
	/** The value for an int to store when a value should be the default from in the preferences. */
	private static final int DEFAULT_INT = -2;
	
	/** The value of the user specified time out. */
	private int timeOut;
	
	/** The value of the user specified max child elements property. */
	private int maxChildElements;
	
	/** Whether the user wishes to use the URL override value. */
	private boolean useUrlOverride;
	
	/** The user specified value for the URL override. */
	private URI urlOverride;
	
	/** The default URL when not using the configuration reference. */
	private URI defaultUrl;
	
	/** The options that represent which of the web service settings are enabled. */
	private int options;
	
	/** The supplier of the web service URL preference id. */
	private IWebServicePreferenceSupplier prefSupplier;
	
	public WebServiceSettings(final IWebServicePreferenceSupplier prefSupplier, final int options) {
		this.prefSupplier = prefSupplier;
		this.defaultUrl = null;
		setOptions(options);
		
		timeOut = DEFAULT_INT;
		maxChildElements = DEFAULT_INT;
		useUrlOverride = false;
		urlOverride = null;
	}
	
	public WebServiceSettings(final IWebServicePreferenceSupplier prefSupplier) {
		this(prefSupplier, WebServiceSettingsOptions.ALL);
	}
	
	public WebServiceSettings(final WebServiceSettings other, final IWebServicePreferenceSupplier prefSupplier) 		
	{
		super(other);		
		this.prefSupplier = prefSupplier;
		this.defaultUrl = null;
		setOptions(other.options);
		this.timeOut = other.timeOut;
		this.maxChildElements = other.maxChildElements;
		this.useUrlOverride = false;
		this.urlOverride = null;
	}
	
	public WebServiceSettings(final AuthenticationSettings other) 		
	{
		super(other);		
		this.defaultUrl = null;
		this.prefSupplier = null;
		this.useUrlOverride = false;
		this.urlOverride = null;
	}
	
	/** @deprecated @see IWebServicePreferenceSupplier. */
	@Deprecated
	public WebServiceSettings(final URI defaultUrl, final int options) 
	{		
		this.defaultUrl = defaultUrl;
		this.prefSupplier = null;
		setOptions(options);
		
		timeOut = DEFAULT_INT;
		maxChildElements = DEFAULT_INT;
		useUrlOverride = false;
		urlOverride = null;	
	}
	
	/** @deprecated @see IWebServicePreferenceSupplier. */
	@Deprecated
	public WebServiceSettings(final URI defaultUrl) {
		this(defaultUrl, WebServiceSettingsOptions.ALL);
	}	
	
	/** @deprecated @see IWebServicePreferenceSupplier. */
	@Deprecated
	public WebServiceSettings(final WebServiceSettings other, URI defaultUrl) 		
	{
		super(other);		
		this.defaultUrl = defaultUrl;
		this.prefSupplier = null;
		setOptions(other.options);		
		this.timeOut = other.timeOut;
		this.maxChildElements = other.maxChildElements;
		this.useUrlOverride = false;
		this.urlOverride = null;
	}
	
	/** @return The options that are enabled for this web service. */
	public int getOptions( ) {
		return options;
	}
	
	/** Set which of the web service settings are enabled.
	 * @param options The settings to enable/disable. */
	public void setOptions(final int options) {
		if(options < 0) {
			this.options = WebServiceSettingsOptions.ALL;
		} else { 
			this.options = options;
		}
	}
	
	/** @return The user specified time out. */
	public int getTimeOut( ) {
		if(timeOut == DEFAULT_INT) {
			return WebServiceClient.getPreferenceTimeOut( );
		} else {
			return timeOut;
		}
	}
	
	/** @return The user specified max child elements. */
	public int getMaxChildElements( ) {
		if(maxChildElements == DEFAULT_INT) {
			return WebServiceClient.getPreferenceMaxChildElements( );
		} else {
			return maxChildElements;
		}
	}
	
	/** @return Whether the URL specified in the tab should be used instead of the preference 
	 * option. */
	public boolean isUseURLOverride( ) {
		return useUrlOverride;
	}
	
	/** @return The value for the URL override. 
	 * @throws WebServiceException */
	public URI getURLOverride( ) throws WebServiceException {
		if(!useUrlOverride || urlOverride == null) {			
			// use the default custom value
			return getDefaultURI();		
		}
		
		return urlOverride;
	}
		
	/** Set the value of the user specified time out.
	 * @param timeOut The new time out value.
	 * @return This, allowing mutator methods to be chained. */
	public WebServiceSettings setTimeOut(final int timeOut) {
		if(timeOut == WebServiceClient.getPreferenceTimeOut( )) {
			this.timeOut = DEFAULT_INT;
		} else {
			this.timeOut = timeOut;
		}
		
		return this;
	}
	
	/** Set the value of the user specified max child elements value.
	 * @param maxChildElements The new max child elements value. 
	 * @return This, allowing mutator methods to be chained. */
	public WebServiceSettings setMaxChildElements(final int maxChildElements) {
		if(maxChildElements == WebServiceClient.getPreferenceMaxChildElements( )) {
			this.maxChildElements = DEFAULT_INT;
		} else {
			this.maxChildElements = maxChildElements;
		}
		
		return this;
	}
	
	/** Set whether to use the URL stored in the settings rather than the value from the 
	 * preferences.
	 * @param useUrlOverride The new use URL override value.
	 * @return This, allowing mutator methods to be chained. */
	public WebServiceSettings setUseURLOverride(final boolean useUrlOverride) {
		this.useUrlOverride = useUrlOverride;
		
		return this;
	}
	
	/** Set the URL to use instead of the value from the preferences.
	 * @param urlOverride The new URL to use.
	 * @return This, allowing mutator methods to be chained. 
	 * @throws WebServiceException */
	public WebServiceSettings setURLOverride(final String urlOverride) throws WebServiceException {
		if(urlOverride == null || "".equals(urlOverride)) {
			this.urlOverride = null;		
		} else if(getDefaultURI() != null && getDefaultURI().equals(urlOverride)) {
			this.urlOverride = null;
		} else {
			this.urlOverride = createURI(urlOverride);
		}
		
		return this;
	}
	
	/** @return Whether the settings stored for the Web Service tab have their default values. */
	public boolean isDefault( ) {
		return timeOut == DEFAULT_INT
				&& maxChildElements == DEFAULT_INT
				&& !useUrlOverride;
	}
	
	/**
	 * @return The preference supplier used by this service settings.
	 */
	public IWebServicePreferenceSupplier getPreferenceSupplier() {
		return prefSupplier;
	}
		
	/** Save the additional properties provided for web service nodes.
	 * @param settings The settings to store the additional properties in. */
	public void saveSettingsTo(final NodeSettingsWO settings) {
		
		if(WebServiceSettingsOptions.useTimeOut(options)) {
			settings.addInt(getSettingId(TIME_OUT_ID), timeOut);
		}
		
		if(WebServiceSettingsOptions.useMaxChildElements(options)) 
		{
			settings.addInt(getSettingId(MAX_CHILD_ELEMENTS_ID), maxChildElements);
		}
		
		if(WebServiceSettingsOptions.useCredentials(options)) {
			super.saveSettingsTo(settings);
		}
		
		settings.addBoolean(getSettingId(USE_URL_OVERRIDE_ID), useUrlOverride);
		if(urlOverride == null) {
			settings.addString(getSettingId(URL_OVERRIDE_ID), DEFAULT_STR);
		} else {
			settings.addString(getSettingId(URL_OVERRIDE_ID), urlOverride.toString( ));
		}
				
	}
	
	private String getSettingId(String s) {
		if (prefSupplier.getKeyPrefix() != null) {
			return prefSupplier.getKeyPrefix() + s;
		}
		return s;
	}
	
	/** Load the additional properties provided for web service nodes.
	 * @param settings The settings to load the additional properties from. */
	public void loadSettingsFrom(final NodeSettingsRO settings) {
		if(WebServiceSettingsOptions.useTimeOut(options)) {
			try {
				timeOut = settings.getInt(getSettingId(TIME_OUT_ID));
			} catch(InvalidSettingsException e) {
				// this value hadn't been stored yet
				timeOut = DEFAULT_INT;
			}
		}
		
		if(WebServiceSettingsOptions.useMaxChildElements(options)) {
			try {
				maxChildElements = settings.getInt(getSettingId(MAX_CHILD_ELEMENTS_ID));
			} catch(InvalidSettingsException e) {
				// the value hasn't been stored yet
				maxChildElements = DEFAULT_INT;
			}
		}
		
		if(WebServiceSettingsOptions.useCredentials(options)) {
			try {
				super.loadSettingsFrom(settings);
			} catch(InvalidSettingsException e) {
				// ignore
			}
		}
		
		try {
			useUrlOverride = settings.getBoolean(getSettingId(USE_URL_OVERRIDE_ID));
			String url = settings.getString(getSettingId(URL_OVERRIDE_ID));
			if(url == null || DEFAULT_STR.equals(url)) {
				urlOverride = null;
			} else {
				urlOverride = createURI(url);
			}
		} catch(InvalidSettingsException e) {
			// value hasn't been stored yet or URL is invalid
			useUrlOverride = false;
			urlOverride = null;
		} catch(WebServiceException e) {
			// the stored URL was invalid
			useUrlOverride = false;
			urlOverride = null;
		}
			
	}
	
	/** Validate the settings values stored in this Object.
	 * @param settings The settings to validate the properties for.
	 * @throws InvalidSettingsException If one or more of the properties is invalid. */
	public void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException 
	{
		loadSettingsFrom(settings);
		
		if(WebServiceSettingsOptions.useTimeOut(options)) {
			if(timeOut != DEFAULT_INT && timeOut <= 0) {
				throw new InvalidSettingsException(
						"Time out value must be a positive non-zero integer."
				);
			}
		}
		
		if(WebServiceSettingsOptions.useMaxChildElements(options)) {
			if(maxChildElements != DEFAULT_INT && maxChildElements <= 0) {
				throw new InvalidSettingsException(
						"Max Child Elements value must be a positive non-zero integer."
				);
			}
		}
		
		try {
			if (getURLOverride() == null) {
				throw new InvalidSettingsException("You must specify a valid Web Service URL.");
			}
		} catch(WebServiceException wse) {
			throw new InvalidSettingsException(wse.getMessage());
		}
	}
	
	@Override
	public boolean equals(final Object o) {
		if(o instanceof WebServiceSettings) {
			WebServiceSettings wss = (WebServiceSettings)o;
			boolean result = timeOut == wss.timeOut 
					&& maxChildElements == wss.maxChildElements;
			
			if(credentialsName != null) {
				result  = result && credentialsName.equals(wss.credentialsName);
			} else {
				result = result && credentialsName == wss.credentialsName;
			}
			
			if(urlOverride != null) {
				result = result && urlOverride.equals(wss.urlOverride);
			} else {
				result = result && urlOverride == wss.urlOverride;
			}
					
			return result;
		}
		return false;
	}
	
	@Override
	public int hashCode( ) {
		int hash = 1;
		hash ^= getTimeOut( );
		hash ^= getMaxChildElements( );
		
		if(getCredentialsName( ) != null) {
			hash ^= getCredentialsName( ).hashCode( );
		}
		
		try {
			if(getURLOverride( ) != null) {
				hash ^= getURLOverride( ).hashCode( );
			}
		} catch(WebServiceException e) {
			// there was an error creating the URL, ignore
		}		
		
		return hash;
	}
	
	
	/** Convert the supplied String into a URI.
	 * @param uri The String to convert.
	 * @return The URI of the String if conversion was successful.
	 * @throws WebServiceException If an error occurs converting the String. */
	private URI createURI(final String uri) throws WebServiceException {
		try {
			return new URI(uri.trim());
		} catch(URISyntaxException e) {
			throw new WebServiceException("Could not build URL from '" + uri + "'.", e);
		}
	}
	
	/** @return The default URI from the preferences. */
	private URI getDefaultURI() {
		// if there is no supplier use the default
		if(prefSupplier == null || prefSupplier.getURLPreferenceId() == null) {
			return defaultUrl;
		}
		
		// otherwise try and retrieve from the preferences
		try {
			URL url = WebServicePreferenceURLRetriever.getDefaultURL(prefSupplier.getURLPreferenceId());			
			URI uri = url.toURI();
			
			if(prefSupplier.getURLSuffix() != null) {
				//	Check to see if we have a trailing slash, if not then add it.
				if (!uri.getPath().endsWith("/")) {
					uri = new URI(uri.toString() + "/");
				}
				uri = uri.resolve(prefSupplier.getURLSuffix());
			}
			
			return uri;
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return defaultUrl;
		}
	}
	
}
