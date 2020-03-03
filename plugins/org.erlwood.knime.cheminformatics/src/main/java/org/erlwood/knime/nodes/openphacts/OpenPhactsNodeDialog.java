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
package org.erlwood.knime.nodes.openphacts;

import java.net.URISyntaxException;

import org.erlwood.knime.CheminformaticsActivator;
import org.erlwood.knime.utils.gui.WebServiceDefaultNodeSettingsPane;
import org.erlwood.knime.utils.settings.WebServiceSettingsOptions;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObjectSpec;

/**
 * <code>NodeDialog</code> for the "OpenPhacts" Node.
 * @author Luke Bullard 
 */
public class OpenPhactsNodeDialog extends WebServiceDefaultNodeSettingsPane {

	/** The tab for the OpenPhacts settings. */
	protected OpenPhactsTab openPhactsTab = new OpenPhactsTab();
	
	/**
	 * Constructor.
	 * @throws URISyntaxException 
	 */
    protected OpenPhactsNodeDialog() {
        super(WebServiceSettingsOptions.REST); 
        removeTab("Options");
        addTabAt(0, "Settings", openPhactsTab);
        setSelected("Settings");
    }
  
    
    /**
     * {@inheritDoc}
     */
    public void loadAdditionalSettingsFrom(final NodeSettingsRO settings, 
            final PortObjectSpec[ ] specs) throws NotConfigurableException 
    {
		super.loadAdditionalSettingsFrom(settings, specs);
		try {
			openPhactsTab.loadSettingsFrom(settings, specs);
		} catch (InvalidSettingsException e) {
			throw new NotConfigurableException(e.getMessage(), e);			
		}
	}

    /**
     * {@inheritDoc}
     */
	@Override
	public void saveAdditionalSettingsTo(final NodeSettingsWO settings)
            throws InvalidSettingsException 
    {
		super.saveAdditionalSettingsTo(settings);
		openPhactsTab.saveSettingsTo(settings);		
	}
	
	@Override
	public String getURLPreferenceId() {
		return CheminformaticsActivator.OPEN_PHACTS_URL;
	}
	
}

