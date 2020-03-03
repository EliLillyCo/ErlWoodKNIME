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
package org.erlwood.knime.utils.gui;

import java.awt.Component;

import org.erlwood.knime.utils.IWebServicePreferenceSupplier;
import org.erlwood.knime.utils.settings.WebServiceSettings;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObjectSpec;

/** A node dialog that implements the required methods for creating the web service settings tab.
 * Extending classes must call super. on the following methods.
 * -- loadSettingsFrom
 * -- saveSettingsTo
 * 
 * @author Tom Wilkin */
public abstract class AbstractWebServiceNodeDialogPane 
		extends NodeDialogPane implements IWebServicePreferenceSupplier
{

	/** The tab for the web service specific settings. */
	private WebServiceDialogTab webServiceTab;
	
	
	protected AbstractWebServiceNodeDialogPane() {
		this(-1);
	}
	
	
	protected AbstractWebServiceNodeDialogPane(final int options)
	{
		// add web service tab
    	webServiceTab = WebServiceDialogTab.createTab(this, options);
    	addTabAt(1, WebServiceDialogTab.TITLE, webServiceTab);
	}
	
	@Override
	public String getURLSuffix() {
		// by default most web service nodes have no suffix
		return null;
	}
	
	@Override
	public String getKeyPrefix() {
		return null;
	}
	
	@Override
	protected final void loadSettingsFrom(final NodeSettingsRO settings, 
			final DataTableSpec[ ] specs) throws NotConfigurableException
	{
		// do not override this method as it will never be called because PortObjectSpec version
		// is overridden
		throw new NotConfigurableException(
				"DataTableSpec loadSettingsFrom method should not be called or implemented, use PortObjectSpec version."
		);
	}
	
	@Override
    protected void loadSettingsFrom(final NodeSettingsRO settings, final PortObjectSpec[ ] specs) 
    		throws NotConfigurableException 
    {
		webServiceTab.loadSettingsFrom(
				settings,
				getAvailableFlowVariables( ),
				getCredentialsProvider( )
		);
    }
	
	@Override
    public void saveSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {
		webServiceTab.saveSettingsTo(settings);
	}
	
	/** Add a new default tab at the beginning of the list and ensure that tab is selected.
	 * @param title The title of the new tab.
	 * @param component The content for the new tab. */
	protected void addDefaultTab(final String title, final Component component) {
		addTabAt(0, title, component);
		setSelected(title);
	}
	
	protected WebServiceSettings getWebServiceSettings() {
		return webServiceTab.getSettings();
	}
};
