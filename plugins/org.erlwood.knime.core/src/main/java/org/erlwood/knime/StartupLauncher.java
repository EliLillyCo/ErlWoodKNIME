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

import org.eclipse.ui.IStartup;
import org.erlwood.knime.utils.debugging.ErlwoodMenuContributions;
import org.knime.core.node.NodeLogger;


/** Class to handle the functions that need to occur at KNIME start-up to configure the Erlwood
 * environment.
 * @author Luke Bullard  
 */
public final class StartupLauncher implements IStartup {
	
	/** Whether the start up methods have already run. */
	private static boolean started = false;
	
	/** The node state change listener to log node execution runtime. */
	private static ErlwoodNodeStateChangeListener stateChangeListener;

	@Override
	public void earlyStartup( ) {   
		start( );
	}
	
	/** Start the methods to initialise the Erlwood environment. */
	public static void start( ) {
		if(!started) {
			started = true;
						
		    // initialise the NodeLogger
		    NodeLogger logger = NodeLogger.getLogger(StartupLauncher.class);
		
		    if(stateChangeListener == null) {
				
				logger.debug("Starting Node State Change Logger");
				stateChangeListener = ErlwoodNodeStateChangeListener.getInstance();				
			}
			
		    //	Add the Erlwood Debugging menus (if needed)
			ErlwoodMenuContributions.addRemoveMenus();
			
		}
	}
	
	/** Unregister listeners created during start-up when bundle is stopped. */
	public static void unregister( ) {
		if(stateChangeListener != null) {
			stateChangeListener.unregister( );
		}
	}
	
};
