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
package org.erlwood.knime.utils.debugging;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.erlwood.knime.CoreActivator;
import org.erlwood.knime.icons.IconLoader;

/**
 * This class handles the addition and management of the Erlwood Debugging
 * contributions.
 * 
 * @author Luke Bullard
 *
 */
public final class ErlwoodMenuContributions {

	private static NodeExecutionStatistics nodeExecutionStatistics;
	private static MemoryProfiler memoryProfiler;

	/**
	 * Adds the menus.
	 */
	private static void addMenus() {
		IWorkbenchWindow wb = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

		if (wb instanceof WorkbenchWindow) {
			MenuManager menuManager = ((WorkbenchWindow) wb).getMenuManager();
			MenuManager helpMenu = (MenuManager) menuManager.find("help");

			ImageDescriptor id = ImageDescriptor.createFromURL(IconLoader.class.getResource("debug.png"));
			MenuManager erlwoodDebuggingMenu = new MenuManager("Debugging", id, "erlwoodDebugging");

			Action nodeExecutionStatsAction = new Action("Node Execution Statistics") {

				@Override
				public void run() {
					if (nodeExecutionStatistics == null) {
						nodeExecutionStatistics = new NodeExecutionStatistics();
					}
					nodeExecutionStatistics.setVisible(true);
					nodeExecutionStatistics.toFront();
				}

			};

			Action memoryProfilerAction = new Action("Memory Profiler") {

				@Override
				public void run() {
					if (memoryProfiler == null) {
						memoryProfiler = new MemoryProfiler();
					}
					memoryProfiler.setVisible(true);
					memoryProfiler.toFront();
				}

			};

			erlwoodDebuggingMenu.add(nodeExecutionStatsAction);
			erlwoodDebuggingMenu.add(memoryProfilerAction);

			erlwoodDebuggingMenu.setVisible(true);
			helpMenu.add(erlwoodDebuggingMenu);

			menuManager.update();
		}
	}

	/**
	 * Removes the menu.
	 */
	private static void removeMenus() {
		IWorkbenchWindow wb = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (wb instanceof WorkbenchWindow) {
			MenuManager menuManager = ((WorkbenchWindow) wb).getMenuManager();
			MenuManager helpMenu = (MenuManager) menuManager.find("help");

			helpMenu.remove("erlwoodDebugging");

			menuManager.update();
		}
	}

	/**
	 * Adds or removes the menu contributions based on the value of
	 * CoreActivator.SHOW_DEBUGGING_MENU
	 */
	public static void addRemoveMenus() {
		addRemoveMenus(CoreActivator.getBoolean(CoreActivator.SHOW_DEBUGGING_MENU));
	}
	
	/**
	 * Adds or removes the menu contributions.
	 * @param show TRUE if you want to show the menu
	 */
	public static void addRemoveMenus(final boolean show) {

		Display.getDefault().asyncExec(new Runnable() {
			public void run() {

				IWorkbenchWindow wb = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				if (wb instanceof WorkbenchWindow) {
					if (show) {
						addMenus();
					} else {
						removeMenus();
					}

				}
			}
		});

	}
}
