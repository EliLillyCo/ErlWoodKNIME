/*
 * ------------------------------------------------------------------------
 *
 * Copyright (C) 2017 Eli Lilly and Company Limited
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
package org.erlwood.knime.utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.erlwood.knime.utils.exceptions.WebServiceException;
import org.knime.core.node.NodeLogger;

/** Class to handle the retrieval of web service URL details from the KNIME preferences UI. 
 * @author Tom Wilkin */
public final class WebServicePreferenceURLRetriever {
	
	/** The NodeLogger instance. **/
	private static final NodeLogger LOG = NodeLogger.getLogger(WebServicePreferenceURLRetriever.class);
	
	/** The extension point id. */
	private static final String EXTENSION_POINT_ID = "org.erlwood.knime.webservice.core.ErlwoodWebServiceURLSupplier";
	
	/** The list of URL suppliers registered against the extension point. */
	private static List<IWebServiceURLSupplier> urlSuppliers;
	
	/** Cannot instantiate static class. */
	private WebServicePreferenceURLRetriever() { }
	
	/** Find the default URL for the given preference id.
	 * @param preferenceId The preference id to find the URL for. 
	 * @return The URL from the preferences.
	 * @throws MalformedURLException If the URL cannot be created from the default/preference value.
	 * @throws WebServiceException If the URL for the preference id cannot be found. */
	public static URL getDefaultURL(final String preferenceId) 
			throws MalformedURLException, WebServiceException
	{
		// find the URL in the URL Suppliers
		String urlStr = null;
		for(IWebServiceURLSupplier supplier : getURLSuppliers()) {
			try {
				urlStr = supplier.getURL(preferenceId);
				if(urlStr != null && !"".equals(urlStr)) {
					break;
				}
			} catch(Exception e) {
				// try the next URL supplier
			}
		}
		
		// check the URL is set
		if(urlStr == null) {
			throw new WebServiceException("Could not find URL in preferences for '" + preferenceId + "'.");
		}
		
		return new URL(urlStr);
	}
	
	/** Check whether the specified preference id is visible in the KNIME preferences.
	 * @param preferenceId The preference id to find the URL for. 
	 * @return Whether this OAuth Token Server URL is visible in the KNIME preferences. */
	public static boolean isURLVisible(final String preferenceId) {
		Boolean visible = null;
		for(IWebServiceURLSupplier supplier : getURLSuppliers()) {
			visible = supplier.isVisible(preferenceId);
			if(visible != null) {
				break;
			}
		}
		
		if(visible == null) {
			return false;
		}
		return visible;
	}
	
	/** @return The list of URL suppliers registered with the extension point. */
	private static List<IWebServiceURLSupplier> getURLSuppliers() {
		if(urlSuppliers == null) {
			// initialise the URL suppliers using the extension point
			urlSuppliers = new ArrayList<>( );
			IExtensionRegistry registry = Platform.getExtensionRegistry( );
			for(IConfigurationElement element : registry.getConfigurationElementsFor(EXTENSION_POINT_ID)) {
				try {
					IWebServiceURLSupplier supplier = (IWebServiceURLSupplier)element.createExecutableExtension("class");
					urlSuppliers.add(supplier);
				} catch(CoreException e) {
					LOG.error(e.getMessage( ), e);
				}
			}
		}
		
		return urlSuppliers;
	}

}
