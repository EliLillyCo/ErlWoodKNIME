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
package org.erlwood.knime;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.erlwood.knime.utils.IWebServiceURLSupplier;
import org.erlwood.knime.utils.exceptions.WebServiceException;
import org.osgi.framework.BundleContext;


public class WebServiceCoreActivator extends AbstractUIPlugin {
	
	/** Class extending the Web Service URL Supplier extension point to supply the OS web service URLs.
	 * @author Tom Wilkin */
	public static final class WebServiceURLSupplier implements IWebServiceURLSupplier {
		
		public WebServiceURLSupplier( ) { }

		@Override
		public String getURL(final String preferenceId) throws WebServiceException {
			if(preferenceId != null) {
				return getString(preferenceId);
			}
			
			throw new WebServiceException("Could not find web service for preference id '" + preferenceId + "'.");
		}

		@Override
		public Boolean isVisible(final String preferenceId) {
			// not required
			return true;
		}
		
	}
		
	/** Preference storage value for the web service timeout. */
    public static final String WEB_SERVICE_TIMEOUT = "web_service_timeout";
    
    /** Preference storage value for the web service max child elements. */
    public static final String WEB_SERVICE_MAX_CHILD_ELEMENTS = "web_service_max_child_elements";
 
	// The shared instance.
	private static WebServiceCoreActivator plugin;
	
	/**
	 * The constructor.
	 */
	public WebServiceCoreActivator() {
		super();
		plugin = this;
	}
	
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);	
	}
	/**
	 * This method is called when the plug-in is stopped.
	 * 
	 * @param context
	 *            The OSGI bundle context
	 * @throws Exception
	 *             If this plugin could not be stopped
	 */
	public void stop(final BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;		
	}

	/**
	 * Returns the shared instance.
	 * 
	 * @return Singleton instance of the Plugin
	 */
	public static WebServiceCoreActivator getDefault() {
		return plugin;
	}

	public static String getString(String name) {
		return getDefault().getPreferenceStore().getString(name);
	}
	
	public static boolean getBoolean(final String name) {
		return getDefault( ).getPreferenceStore( ).getBoolean(name);
	}
	
	public static int getInteger(final String name) {
		return getDefault( ).getPreferenceStore( ).getInt(name);
	}
	
}


