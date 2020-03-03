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
package org.erlwood.knime.nodes.openphacts.settings.compound;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import org.erlwood.knime.utils.settings.AbstractColumnAndValueSettings;

/**
 * Settings container for the GetCompoundPharmacology OpenPhacts call.
 * @author Luke Bullard
 *
 */
public class GetCompoundPharmacologySettings  {
	private URISetting 		uri 		= new URISetting();
	
	public static class URISetting extends AbstractColumnAndValueSettings {

		@Override
		public String getColumnConfigKey() 		{	return "PHARMACOLOGY_URI_COLUMN";		}

		@Override
		public String getValueConfigKey() 		{	return "PHARMACOLOGY_URI_VALUE";		}

		@Override
		protected String getUseColumnConfigKey()	{	return "USE_PHARMACOLOGY_URI_COLUMN";	}
		
		@Override
		protected String getColumnValidationFailMessage() {		
			return "You must specify a valid URI column";
		}

		@Override
		protected String getValueValidationFailMessage() {
			return "You must specify a valid URI value";
		}
		
	};

	
	/** 
	 * Load the settings to this container.
	 * @param settings The settings to load the configuration from. 
	 * @throws InvalidSettingsException 
	 */
	public void loadSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		uri.loadSettingsFrom(settings);			
	}
	
	/** 
	 * Save the settings of this container.
	 * @param settings The settings to store the configuration in. 
	 */
	public void saveSettingsTo(final NodeSettingsWO settings) {
		uri.saveSettingsTo(settings);	
	}

	public URISetting getURISettings() {
		return uri;
	}

	public void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		uri.validateSettings(settings);
	}

	
}


