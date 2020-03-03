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
package org.erlwood.knime.views;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.OpenWindowListener;
import org.eclipse.swt.browser.WindowEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.internal.browser.BrowserViewer;
import org.eclipse.ui.internal.browser.WebBrowserView;
import org.knime.core.node.NodeLogger;
import org.erlwood.knime.CoreActivator;

/**
 * This class simply display the contents of a web page set in the preferences.
 * 
 * @author Luke Bullard
 * 
 */
@SuppressWarnings("restriction")
public class HelpInfo extends WebBrowserView {
	private static final NodeLogger LOG = NodeLogger.getLogger(HelpInfo.class);
	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "org.erlwood.knime.views.HelpInfo";

	@Override
	public void createPartControl(final Composite parent) {

		viewer = new BrowserViewer(parent, 0);
		viewer.setContainer(this);

		// Add a listener to intercept the default process and replace with our
		// own.
		viewer.getBrowser().addOpenWindowListener(new OpenWindowListener() {
			public void open(WindowEvent event) {
				// Get rid if the default browser
				if (event.browser != null) {
					event.browser.dispose();
				}
				// Create our own.
				event.browser = new MyInternalBrowser(parent, 0);
			}
		});

		URL helpURL = Platform.getInstallLocation().getURL();
		try {
			helpURL = new URL(CoreActivator.getString(CoreActivator.WEBVIEWER_URL));
		} catch (MalformedURLException ex) {
			LOG.error(ex.getMessage(), ex);
		}
		initDragAndDrop();
		super.setURL(helpURL.toString());

		// create a timer to refresh the page at an interval
		int interval = CoreActivator.getInteger(CoreActivator.WEBVIEWER_REFRESH_INTERVAL) * 1000;
		if(CoreActivator.getBoolean(CoreActivator.WEBVIEWER_REFRESH) && interval > 0) {
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					// refresh in the UI thread
					if (!viewer.isDisposed()) {
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								viewer.refresh();
							}
						});
					}
				}
			}, interval, interval);
		}
	}

	private class MyInternalBrowser extends Browser {
		public MyInternalBrowser(Composite parent, int style) {
			super(parent, style);
		}
	}

}