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

import org.apache.cxf.transports.http.configuration.ProxyServerType;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;



public class CoreActivator extends AbstractUIPlugin {

	public static final String NT_CREDS = "NT_CREDS";

	public static final String NT_USERNAME = "NT_USERNAME";

	public static final String NT_PASSWORD = "NT_PASSWORD";

	public static final String WORKFLOW_CREDENTIALS = "WORKFLOW_CREDENTIALS";
	
	// Web Viewer preferences
	public static final String WEBVIEWER_URL = "WEBVIEWER_URL";
	
	public static final String WEBVIEWER_REFRESH = "WEBVIEWER_REFRESH";
	
	public static final String WEBVIEWER_REFRESH_INTERVAL = "WEBVIEWER_REFRESH_INTERVAL"; 
    
	//	Debugging Preferences
	public static final String SHOW_DEBUGGING_MENU = "SHOW_DEBUGGING_MENU";
	
	// The shared instance.
	private static CoreActivator plugin;
	
	/**
	 * The constructor.
	 */
	public CoreActivator() {
		super();
		plugin = this;
		ProxyServerType.values();
	}
	
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);	
				
		// disable logging of POI. To speed it up.
		System.setProperty("org.apache.poi.util.POILogger",
                "org.apache.poi.util.NullLogger");
		
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
	public static CoreActivator getDefault() {
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


