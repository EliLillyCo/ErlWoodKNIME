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

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.knime.core.node.NodeLogger;



public class PreferenceInitializer extends AbstractPreferenceInitializer {
	private static final NodeLogger LOG = NodeLogger.getLogger(PreferenceInitializer.class);
	
	@Override
	public void initializeDefaultPreferences() 
	{
		IPreferenceStore store = CoreActivator.getDefault().getPreferenceStore();
			
		LOG.info("Setting Preferences");
		
		store.setDefault(CoreActivator.NT_CREDS, "knime.ntcreds");	
		store.setDefault(CoreActivator.NT_USERNAME, "knime.ntuser");
		store.setDefault(CoreActivator.NT_PASSWORD, "knime.ntpass");
		store.setDefault(CoreActivator.WORKFLOW_CREDENTIALS, "knime.system.default");
		
		// Web Viewer Preferences
		store.setDefault(CoreActivator.WEBVIEWER_URL, "http://tech.knime.org/forum");
		store.setDefault(CoreActivator.WEBVIEWER_REFRESH, false);
		store.setDefault(CoreActivator.WEBVIEWER_REFRESH_INTERVAL, 0);		
			
		LOG.info("Finished Preferences");

	}
	
	/** Return the integer stored with the specified id.
	 * @param id The id of the integer from the preferences to return.
	 * @return The integer from the preferences with the specified id. */
	public static int getInt(final String id) {
		return CoreActivator.getDefault( ).getPreferenceStore( ).getInt(id);
	}
	
	/** Return the default value of the integer stored with the specified id.
	 * @param id The id of the integer from the preferences to return.
	 * @return The default value of the integer from the preferences with the specified id. */
	public static int getDefaultInt(final String id) {
		return CoreActivator.getDefault( ).getPreferenceStore( ).getDefaultInt(id);
	}
}
