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

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;



public class CheminformaticsActivator extends AbstractUIPlugin {
	
	/** OpenPhacts Settings **/
	public static final String OPEN_PHACTS_APP_ID 	= "OPEN_PHACTS_APP_ID";
	public static final String OPEN_PHACTS_APP_KEY 	= "OPEN_PHACTS_APP_KEY";
	public static final String OPEN_PHACTS_URL 		= "OPEN_PHACTS_URL";


	// The shared instance.
	private static final CheminformaticsActivator plugin = new CheminformaticsActivator();
	
	private String previousTableUI = null;

	
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		
		//	In order to support the use of Transferable objects to the clipboard we need
		//	 to take over the standard UI for tables.
//		if(!System.getProperty("os.name").toLowerCase( ).contains("mac")) {
//			SwingUtilities.invokeLater(() -> {
//				previousTableUI = (String)UIManager.get("TableUI");
//				
//				UIManager.getDefaults().put("TableUI", "org.erlwood.knime.utils.gui.TransferableBasicTableUI");
//				UIManager.getDefaults().getUIClass("TableUI");
//			});
//			
//		}				
		
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
	
		//	Restore UI
		if(!System.getProperty("os.name").toLowerCase( ).contains("mac")) {
			SwingUtilities.invokeLater(() -> UIManager.getLookAndFeelDefaults().put("TableUI", previousTableUI));
		}		
	}

	/**
	 * Returns the shared instance.
	 * 
	 * @return Singleton instance of the Plugin
	 */
	public static CheminformaticsActivator getDefault() {
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


