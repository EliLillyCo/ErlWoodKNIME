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
package org.erlwood.knime.nodes.openphacts.ui.classes;

import org.erlwood.knime.utils.gui.layout.TableLayout;
import org.knime.core.node.port.PortObjectSpec;

import org.erlwood.knime.nodes.openphacts.OpenPhactsNodeModel;
import org.erlwood.knime.nodes.openphacts.OpenPhactsNodeModel.eMethod;
import org.erlwood.knime.nodes.openphacts.settings.OpenPhactsSettings;
import org.erlwood.knime.utils.settings.ui.AbstractSettingsPanel;
import org.erlwood.knime.utils.settings.ui.ISettingsPanel;

/**
 * Panel used to configure the GetRootClasses OpenPhacts call.
 * @author Luke Bullard
 *
 */
public class GetRootClasses extends AbstractSettingsPanel implements ISettingsPanel<OpenPhactsNodeModel.eMethod, OpenPhactsSettings> {
	/**
	 * Serial Id.
	 */
	private static final long serialVersionUID = 1L;
		
	/**
	 * Constructor.
	 */
	public GetRootClasses() {	
		
		setLayout(new TableLayout(new double[][] {{TableLayout.FILL}, {TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5, TableLayout.PREFERRED, TableLayout.FILL}}));
	
		
	}
	
		
	/**
	 * {@inheritDoc}
	 */
	@Override
	public eMethod getMethod() {
		return eMethod.GetRootClasses;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateSettings(OpenPhactsSettings settings) {				
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateUI(final OpenPhactsSettings settings, final PortObjectSpec[ ] specs) {		
	}

}
