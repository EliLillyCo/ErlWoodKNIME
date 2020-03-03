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

import org.erlwood.knime.nodes.openphacts.exec.chemicalstructuresearch.GetCompoundsBySimilarityExec.eSimilarityType;
import org.erlwood.knime.utils.settings.AbstractColumnAndValueSettings;
import org.erlwood.knime.utils.settings.AbstractValueSettings;

/**
 * Settings container for the GetCompoundsBySimilarity OpenPhacts call.
 * @author Luke Bullard
 *
 */
public class GetCompoundsBySimilaritySettings  {
	private SmilesSetting 			smiles 			= new SmilesSetting();
	private SimilarityTypeSetting 	similarityType 	= new SimilarityTypeSetting();
	private ThresholdSetting 		threshold 		= new ThresholdSetting();
	private TverskyASetting			tverskyA        = new TverskyASetting();
	private TverskyBSetting			tverskyB        = new TverskyBSetting();
	
	public static class SmilesSetting extends AbstractColumnAndValueSettings {

		@Override
		public String getColumnConfigKey() 		{	return "SIMILARITY_SMILES_COLUMN";		}

		@Override
		public String getValueConfigKey() 		{	return "SIMILARITY_SMILES_VALUE";		}

		@Override
		protected String getUseColumnConfigKey()	{	return "USE_SIMILARITY_SMILES_COLUMN";	}
		
		@Override
		protected String getColumnValidationFailMessage() {		
			return "You must specify a valid Smiles column";
		}

		@Override
		protected String getValueValidationFailMessage() {
			return "You must specify a valid Smiles value";
		}
		
	};
	
	public static class SimilarityTypeSetting extends AbstractValueSettings  {


		@Override
		public String getValueConfigKey() 		{	return "SIMILARITY_TYPE_VALUE";		}


		@Override
		protected String getValidationFailMessage() {
			return "You must specify a valid Similarity Type";
		}
		
	};
	
	public static class ThresholdSetting extends AbstractValueSettings {
	
		@Override
		public String getValueConfigKey() 		{	return "SIMILARITY_THRESHOLD_VALUE";		}

		@Override
		protected String getValidationFailMessage() {
			return "You must specify a valid Threshold value";
		}
		
		@Override
		protected boolean validateValue(String v) {
			return validateDoubleValue(v, 0, 1, false);
		}
	};
	
	public static class TverskyASetting extends AbstractValueSettings {
		
		@Override
		public String getValueConfigKey() 		{	return "SIMILARITY_TVERSKY_A_VALUE";		}

		@Override
		protected String getValidationFailMessage() {
			return "You must specify a valid Tversky A value";
		}
		
		@Override
		protected String getDefaultValue() {		
			return "0.5";
		}
		
		@Override
		protected boolean validateValue(String v) {
			return validateDoubleValue(v, 0, 1, false);
		}
	};
	
	public static class TverskyBSetting extends AbstractValueSettings {
		
		@Override
		public String getValueConfigKey() 		{	return "SIMILARITY_TVERSKY_B_VALUE";		}

		@Override
		protected String getValidationFailMessage() {
			return "You must specify a valid Tversky B value";
		}
		
		@Override
		protected String getDefaultValue() {		
			return "0.5";
		}
		
		@Override
		protected boolean validateValue(String v) {
			return validateDoubleValue(v, 0, 1, false);
		}
	};
	
	/** 
	 * Load the settings to this container.
	 * @param settings The settings to load the configuration from. 
	 * @throws InvalidSettingsException 
	 */
	public void loadSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		smiles.loadSettingsFrom(settings);
		similarityType.loadSettingsFrom(settings);
		threshold.loadSettingsFrom(settings);
		tverskyA.loadSettingsFrom(settings);
		tverskyB.loadSettingsFrom(settings);
				
	}
	
	/** 
	 * Save the settings of this container.
	 * @param settings The settings to store the configuration in. 
	 */
	public void saveSettingsTo(final NodeSettingsWO settings) {
		smiles.saveSettingsTo(settings);
		similarityType.saveSettingsTo(settings);
		threshold.saveSettingsTo(settings);
		tverskyA.saveSettingsTo(settings);
		tverskyB.saveSettingsTo(settings);
	}
	
	public void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		smiles.validateSettings(settings);
		similarityType.validateSettings(settings);
		threshold.validateSettings(settings);
		
		if (eSimilarityType.valueOf(settings.getString(similarityType.getValueConfigKey())).equals(eSimilarityType.Tversky)) {
			tverskyA.validateSettings(settings);
			tverskyB.validateSettings(settings);
		}
	}
	
	public SmilesSetting getSmilesSettings() {
		return smiles;
	}

	public SimilarityTypeSetting getSimilarityTypeSettings() {
		return similarityType;
	}

	public ThresholdSetting getThresholdSetting() {
		return threshold;
	}
	
	public TverskyASetting getTverskyASetting() {
		return tverskyA;
	}
	
	public TverskyBSetting getTverskyBSetting() {
		return tverskyB;
	}
	
}


