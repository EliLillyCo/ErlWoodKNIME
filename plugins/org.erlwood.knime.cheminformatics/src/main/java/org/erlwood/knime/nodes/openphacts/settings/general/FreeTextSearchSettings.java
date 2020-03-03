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
package org.erlwood.knime.nodes.openphacts.settings.general;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import org.erlwood.knime.utils.settings.AbstractColumnAndValueSettings;

/**
 * Settings container for the FreeTexSearch call.
 * @author Luke Bullard
 *
 */
public class FreeTextSearchSettings  {
	private FreeTextSetting 		freeText 		= new FreeTextSetting();
	
	public static class FreeTextSetting extends AbstractColumnAndValueSettings {

		@Override
		public String getColumnConfigKey() 		{	return "FREE_TEXT_COLUMN";		}

		@Override
		public String getValueConfigKey() 		{	return "FREE_TEXT_VALUE";		}

		@Override
		protected String getUseColumnConfigKey()	{	return "USE_FREE_TEXT_COLUMN";	}
		
		@Override
		protected String getColumnValidationFailMessage() {		
			return "You must specify a valid Free Text column";
		}

		@Override
		protected String getValueValidationFailMessage() {
			return "You must specify a valid Free Text value";
		}		
	};

	
	/** 
	 * Load the settings to this container.
	 * @param settings The settings to load the configuration from. 
	 * @throws InvalidSettingsException 
	 */
	public void loadSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		freeText.loadSettingsFrom(settings);			
	}
	
	/** 
	 * Save the settings of this container.
	 * @param settings The settings to store the configuration in. 
	 */
	public void saveSettingsTo(final NodeSettingsWO settings) {
		freeText.saveSettingsTo(settings);	
	}

	public FreeTextSetting getFreeTextSettings() {
		return freeText;
	}

	public void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		freeText.validateSettings(settings);
	}

	
}


