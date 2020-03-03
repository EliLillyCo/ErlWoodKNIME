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

import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;

/** Shared methods for the NodeDialogs used by the Desirability and Pareto Ranking nodes.
 * @author Tom Wilkin */
public abstract class RankingNodeDialog extends DefaultNodeSettingsPane {

	/** The settings where the configuration is stored. */
	private SettingsModelString mSettings;
	
	/** The current table specification for the compatible columns in the input. */
	private DataTableSpec mLastSpec;
	
	/** Construct a new RankingNodeDialog.
	 * @param settingsName The name of the settings object in the saved output. */
	protected RankingNodeDialog(final String settingsName) {
		mSettings = new SettingsModelString(settingsName, null);
		mLastSpec = null;
	}
	
	/** Whether the current table specification contains the specified column.
	 * @param columnName The column name to check for in the current table specification.
	 * @return Whether the current table specification contains the specified column. */
	public boolean containsColumn(final String columnName) {
		if(mLastSpec == null) {
			return false;
		}
		
		if(mLastSpec.getColumnSpec(columnName) != null) {
			return true;
		}
		return false;
	}
	
	@Override
	public void loadAdditionalSettingsFrom(final NodeSettingsRO settings, 
			final PortObjectSpec[ ] specs) throws NotConfigurableException
	{
		try {
			mSettings.loadSettingsFrom(settings);
		} catch (InvalidSettingsException e) {
			// ignore
		}
		mLastSpec = (DataTableSpec) specs[0];
	}
	
	/** Allow saving of the property provided by this class.
	 * @param configStr The configuration string from the panel in the dialog. */
	protected void saveAdditionalSettingsTo(final NodeSettingsWO settings, final String configStr) 
			throws InvalidSettingsException 
	{
		mSettings.setStringValue(configStr);
		mSettings.saveSettingsTo(settings);
	}
	
	/** Generate the configuration string using the currently configured parameters and the 
	 * available properties from the current table specification.
	 * @return The configuration string. */
	protected String getConfigString( ) {
		String configured = getConfiguredProps( );
		String available = getAvailableProps(configured);
		return available + configured;
	}
	
	/** @return The configured properties the user has enabled for ranking. */
	private String getConfiguredProps( ) {
		StringBuffer sb = new StringBuffer( );
		
		String currentValue = mSettings.getStringValue( );
		if(currentValue != null) {
			String[ ] props = currentValue.split("\n");
			for(String prop : props) {
				if(prop.startsWith("configured_prop=")) {
					sb.append(prop).append("\n");
				}
			}
		}
		
		return sb.toString( );
	}
	
	/** The available properties that the user can rank. */
	private String getAvailableProps(final String configuredProps) {
		StringBuffer sb = new StringBuffer( );
		
		// split the configured props
		String[ ] props = configuredProps.split("\n");
		if(!configuredProps.equals("")) {
			for(int i = 0; i < props.length; i++) {
				props[i] = props[i].substring("configured_prop=".length( ));
				int index = props[i].indexOf(";");
				if(index > 0) {
					props[i] = props[i].substring(0, index);
				}
			}
		}
		
		// iterate through the columns
		if(mLastSpec != null) {
			for(int i = 0; i < mLastSpec.getNumColumns( ); i++) {				
				if(mLastSpec.getColumnSpec(i).getType( ).isCompatible(DoubleValue.class)) {
					// check if this prop has been used already
					boolean alreadyUsed = false;
					for(String prop : props) {
						if(prop.equals(mLastSpec.getColumnSpec(i).getName( ))) {
							alreadyUsed = true;
							break;
						}
					}
					if(!alreadyUsed) {
						sb.append("available_prop=")
							.append(mLastSpec.getColumnSpec(i).getName( ))
							.append("\n");
					}
				}
			}
		}
		
		return sb.toString( );
	}
	
};
