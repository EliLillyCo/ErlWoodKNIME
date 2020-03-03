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
package org.erlwood.knime.nodes.openphacts.settings.chemicalstructuresearch;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import org.erlwood.knime.utils.settings.AbstractColumnAndValueSettings;
import org.erlwood.knime.utils.settings.AbstractValueSettings;

/**
 * Settings container for the GetCompoundURI OpenPhacts call.
 * @author Luke Bullard
 *
 */
public class GetCompoundURISettings  {
	private SmilesSetting 		smiles 		= new SmilesSetting();
	private MatchTypeSetting 	matchType	= new MatchTypeSetting();
	
	public static class SmilesSetting extends AbstractColumnAndValueSettings {

		@Override
		public String getColumnConfigKey() 		{	return "GET_COMPOUND_URI_SMILES_COLUMN";		}

		@Override
		public String getValueConfigKey() 		{	return "GET_COMPOUND_URI_SMILES_VALUE";		}

		@Override
		protected String getUseColumnConfigKey()	{	return "USE_GET_COMPOUND_URI_SMILES_COLUMN";	}
		
		@Override
		protected String getColumnValidationFailMessage() {		
			return "You must specify a valid Smiles column";
		}

		@Override
		protected String getValueValidationFailMessage() {
			return "You must specify a valid Smiles value";
		}		
	};
	
	public static class MatchTypeSetting extends AbstractValueSettings {

		@Override
		public String getValueConfigKey() 		{	return "GET_COMPOUND_URI_MATCH_TYPE_VALUE";		}
		
		@Override
		protected String getValidationFailMessage() {		
			return "You must specify a valid Match Type";
		}
		
	};

	
	/** 
	 * Load the settings to this container.
	 * @param settings The settings to load the configuration from. 
	 * @throws InvalidSettingsException 
	 */
	public void loadSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		smiles.loadSettingsFrom(settings);
		matchType.loadSettingsFrom(settings);
	}
	
	/** 
	 * Save the settings of this container.
	 * @param settings The settings to store the configuration in. 
	 */
	public void saveSettingsTo(final NodeSettingsWO settings) {
		smiles.saveSettingsTo(settings);
		matchType.saveSettingsTo(settings);
	}

	public void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		smiles.validateSettings(settings);
		matchType.validateSettings(settings);
	}
	
	public SmilesSetting getSmilesSettings() {
		return smiles;
	}

	public MatchTypeSetting getMatchTypeSetting() {
		return matchType;
	}
	

	
}


