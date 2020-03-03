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
package org.erlwood.knime.nodes.openphacts.settings;

import org.erlwood.knime.CheminformaticsActivator;
import org.erlwood.knime.WebServiceCoreActivator;
import org.erlwood.knime.nodes.openphacts.OpenPhactsNodeModel;
import org.erlwood.knime.nodes.openphacts.settings.assay.GetAssaySettings;
import org.erlwood.knime.nodes.openphacts.settings.chemicalstructuresearch.GetCompoundURISettings;
import org.erlwood.knime.nodes.openphacts.settings.chemicalstructuresearch.GetCompoundsBySimilaritySettings;
import org.erlwood.knime.nodes.openphacts.settings.chemicalstructuresearch.GetCompoundsBySubstructureSettings;
import org.erlwood.knime.nodes.openphacts.settings.classes.GetChildClassesSettings;
import org.erlwood.knime.nodes.openphacts.settings.classes.GetParentClassesSettings;
import org.erlwood.knime.nodes.openphacts.settings.compound.GetCompoundClassesSettings;
import org.erlwood.knime.nodes.openphacts.settings.compound.GetCompoundPharmacologySettings;
import org.erlwood.knime.nodes.openphacts.settings.compound.GetCompoundSettings;
import org.erlwood.knime.nodes.openphacts.settings.compound.GetCompoundsByClassSettings;
import org.erlwood.knime.nodes.openphacts.settings.disease.GetDiseaseAssociationsSettings;
import org.erlwood.knime.nodes.openphacts.settings.disease.GetDiseaseSettings;
import org.erlwood.knime.nodes.openphacts.settings.disease.GetDiseasesByTargetSettings;
import org.erlwood.knime.nodes.openphacts.settings.disease.GetTargetAssociationsSettings;
import org.erlwood.knime.nodes.openphacts.settings.disease.GetTargetsByDiseaseSettings;
import org.erlwood.knime.nodes.openphacts.settings.general.FreeTextSearchSettings;
import org.erlwood.knime.nodes.openphacts.settings.target.GetTargetClassesSettings;
import org.erlwood.knime.nodes.openphacts.settings.target.GetTargetCompoundClassesSettings;
import org.erlwood.knime.nodes.openphacts.settings.target.GetTargetPharmacologyByClassSettings;
import org.erlwood.knime.nodes.openphacts.settings.target.GetTargetPharmacologySettings;
import org.erlwood.knime.nodes.openphacts.settings.target.GetTargetSettings;
import org.erlwood.knime.nodes.openphacts.settings.target.GetTargetsByClassSettings;
import org.erlwood.knime.utils.settings.AbstractSettings;
import org.erlwood.knime.utils.settings.AbstractValueSettings;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
/**
 * Settings class for holding the OpenPhacts values.
 * @author Luke Bullard
 *
 */
public class OpenPhactsSettings extends AbstractSettings<OpenPhactsNodeModel.eMethod> {
	private ApplicationIdSetting				appId		= new ApplicationIdSetting();
	private ApplicationKeySetting				appKey		= new ApplicationKeySetting();
	private ReturnFormatTypeSetting				format		= new ReturnFormatTypeSetting();
	
	private FreeTextSearchSettings				freeTextSearchSettings				= new FreeTextSearchSettings();
	private GetAssaySettings					getAssaySettings 					= new GetAssaySettings();
	
	private GetCompoundURISettings				getCompoundURISettings 				= new GetCompoundURISettings();
	private GetCompoundsBySimilaritySettings	getCompoundsBySimilaritySettings 	= new GetCompoundsBySimilaritySettings();
	private GetCompoundsBySubstructureSettings	getCompoundsBySubstructureSettings 	= new GetCompoundsBySubstructureSettings();
	
	private GetCompoundSettings					getCompoundSettings 				= new GetCompoundSettings();
	private GetCompoundClassesSettings			getCompoundClassesSettings 			= new GetCompoundClassesSettings();
	private GetCompoundsByClassSettings			getCompoundsByClassSettings			= new GetCompoundsByClassSettings();
	private GetCompoundPharmacologySettings 	getCompoundPharmacologySettings     = new GetCompoundPharmacologySettings();
	
	private GetChildClassesSettings 			getChildClassesSettings     		= new GetChildClassesSettings();
	private GetParentClassesSettings 			getParentClassesSettings     		= new GetParentClassesSettings();
	
	private GetDiseaseSettings 					getDiseaseSettings		     		= new GetDiseaseSettings();
	private GetDiseaseAssociationsSettings 		getDiseaseAssociationsSettings		= new GetDiseaseAssociationsSettings();
	private GetTargetAssociationsSettings 		getTargetAssociationsSettings		= new GetTargetAssociationsSettings();
	private GetDiseasesByTargetSettings 		getDiseasesByTargetSettings		    = new GetDiseasesByTargetSettings();
	private GetTargetsByDiseaseSettings 		getTargetsByDiseaseSettings		    = new GetTargetsByDiseaseSettings();
	
	private GetTargetSettings 					getTargetSettings				    = new GetTargetSettings();
	private GetTargetClassesSettings 			getTargetClassesSettings		    = new GetTargetClassesSettings();
	private GetTargetCompoundClassesSettings 	getTargetCompoundClassesSettings	= new GetTargetCompoundClassesSettings();
	private GetTargetsByClassSettings 			getTargetsByClassesSettings		    = new GetTargetsByClassSettings();
	private GetTargetPharmacologySettings 		getTargetPharmacologySettings		= new GetTargetPharmacologySettings();
	private GetTargetPharmacologyByClassSettings	getTargetPharmacologyByClassSettings		= new GetTargetPharmacologyByClassSettings();
	
	public static class ApplicationIdSetting extends AbstractValueSettings {
		
		@Override
		public String getValueConfigKey() 		{	return "APPLICATION_ID_VALUE";		}

		@Override
		protected String getValidationFailMessage() {
			return "You must specify a valid Application Id value";
		}		
		
		@Override
		protected String getDefaultValue() {
			return WebServiceCoreActivator.getString(CheminformaticsActivator.OPEN_PHACTS_APP_ID);
		}
	};
	
	public static class ApplicationKeySetting extends AbstractValueSettings {
		
		@Override
		public String getValueConfigKey() 		{	return "APPLICATION_KEY_VALUE";		}

		@Override
		protected String getValidationFailMessage() {
			return "You must specify a valid Application Key value";
		}
		
		@Override
		protected String getDefaultValue() {
			return WebServiceCoreActivator.getString(CheminformaticsActivator.OPEN_PHACTS_APP_KEY);
		}
	};
	
	public static class ReturnFormatTypeSetting extends AbstractValueSettings {
		
		@Override
		public String getValueConfigKey() 		{	return "RETURN_FOMAT_TYPE_VALUE";		}

		@Override
		protected String getValidationFailMessage() {
			return "You must specify a valid Return Format Type";
		}
		
		@Override
		protected String getDefaultValue() {
			return OpenPhactsNodeModel.eFormat.NONE.name();
		}
	};
	
	private boolean bAppend;
		
	/** Load the settings to this dialog tab.
	 * @param settings The settings to load the configuration from. 
	 * @throws InvalidSettingsException */
	public void loadSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		super.loadSettingsFrom(settings);
		
		appId.loadSettingsFrom(settings);
		appKey.loadSettingsFrom(settings);
		format.loadSettingsFrom(settings);
		
		
		// load the settings
		getAssaySettings.loadSettingsFrom(settings);
		
		getCompoundURISettings.loadSettingsFrom(settings);		
		getCompoundsBySimilaritySettings.loadSettingsFrom(settings);		
		getCompoundsBySubstructureSettings.loadSettingsFrom(settings);
		
		getCompoundSettings.loadSettingsFrom(settings);
		getCompoundClassesSettings.loadSettingsFrom(settings);
		getCompoundsByClassSettings.loadSettingsFrom(settings);
		getCompoundPharmacologySettings.loadSettingsFrom(settings);
		
		getChildClassesSettings.loadSettingsFrom(settings);
		getParentClassesSettings.loadSettingsFrom(settings);
		
		getDiseaseSettings.loadSettingsFrom(settings);
		getDiseaseAssociationsSettings.loadSettingsFrom(settings);
		
		getTargetAssociationsSettings.loadSettingsFrom(settings);
		
		getDiseasesByTargetSettings.loadSettingsFrom(settings);
		getTargetsByDiseaseSettings.loadSettingsFrom(settings);
		
		getTargetSettings.loadSettingsFrom(settings);
		getTargetClassesSettings.loadSettingsFrom(settings);
		getTargetCompoundClassesSettings.loadSettingsFrom(settings);
		getTargetsByClassesSettings.loadSettingsFrom(settings);
		getTargetPharmacologySettings.loadSettingsFrom(settings);
		getTargetPharmacologyByClassSettings.loadSettingsFrom(settings);
		
		freeTextSearchSettings.loadSettingsFrom(settings);
		
		bAppend = settings.getBoolean("append", false);
	}
	
	/** Save the settings of this dialog tab.
	 * @param settings The settings to store the configuration in. */
	public void saveSettingsTo(final NodeSettingsWO settings) {
		super.saveSettingsTo(settings);
	
		appId.saveSettingsTo(settings);
		appKey.saveSettingsTo(settings);
		format.saveSettingsTo(settings);
		
		getAssaySettings.saveSettingsTo(settings);
		
		getCompoundURISettings.saveSettingsTo(settings);		
		getCompoundsBySimilaritySettings.saveSettingsTo(settings);	
		getCompoundsBySubstructureSettings.saveSettingsTo(settings);
		
		getCompoundSettings.saveSettingsTo(settings);
		getCompoundClassesSettings.saveSettingsTo(settings);
		getCompoundsByClassSettings.saveSettingsTo(settings);
		getCompoundPharmacologySettings.saveSettingsTo(settings);
		
		getChildClassesSettings.saveSettingsTo(settings);
		getParentClassesSettings.saveSettingsTo(settings);
		
		getDiseaseSettings.saveSettingsTo(settings);
		getDiseaseAssociationsSettings.saveSettingsTo(settings);
		
		getTargetAssociationsSettings.saveSettingsTo(settings);
		getDiseasesByTargetSettings.saveSettingsTo(settings);
		getTargetsByDiseaseSettings.saveSettingsTo(settings);
		
		getTargetSettings.saveSettingsTo(settings);
		getTargetClassesSettings.saveSettingsTo(settings);
		getTargetCompoundClassesSettings.saveSettingsTo(settings);
		getTargetsByClassesSettings.saveSettingsTo(settings);
		getTargetPharmacologySettings.saveSettingsTo(settings);
		getTargetPharmacologyByClassSettings.saveSettingsTo(settings);
		
		freeTextSearchSettings.saveSettingsTo(settings);
		
		
		settings.addBoolean("append", bAppend);
	}
	
	public void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		appId.validateSettings(settings);
		appKey.validateSettings(settings);
		format.validateSettings(settings);
		
		//		Grab the method
		String s = settings.getString(METHOD, null);
		
		switch(OpenPhactsNodeModel.eMethod.valueOf(s)) {
			case GetAssay:
				getAssaySettings.validateSettings(settings);
				break;
			case GetCompoundURI:
				getCompoundURISettings.validateSettings(settings);
				break;
			case GetCompoundsBySimilarity:
				getCompoundsBySimilaritySettings.validateSettings(settings);
				break;
			case GetCompoundsBySubstructure:
				getCompoundsBySubstructureSettings.validateSettings(settings);
				break;
			case GetCompound:
				getCompoundSettings.validateSettings(settings);
				break;
			case GetCompoundClasses:
				getCompoundClassesSettings.validateSettings(settings);
				break;
			case GetCompoundsByClass:
				getCompoundsByClassSettings.validateSettings(settings);
				break;
			case GetCompoundPharmacology:
				getCompoundPharmacologySettings.validateSettings(settings);
				break;
			case GetChildClasses:
				getChildClassesSettings.validateSettings(settings);
				break;
			case GetParentClasses:
				getParentClassesSettings.validateSettings(settings);
				break;
			case GetDisease:
				getDiseaseSettings.validateSettings(settings);
				break;
			case GetDiseaseAssociations:
				getDiseaseAssociationsSettings.validateSettings(settings);
				break;
			case GetTargetAssociations:
				getTargetAssociationsSettings.validateSettings(settings);
				break;
			case GetDiseasesByTarget:
				getDiseasesByTargetSettings.validateSettings(settings);
				break;
			case GetTargetsByDisease:
				getTargetsByDiseaseSettings.validateSettings(settings);
				break;
			case GetTarget:
				getTargetSettings.validateSettings(settings);
				break;
			case GetTargetClasses:
				getTargetClassesSettings.validateSettings(settings);
				break;
			case GetTargetCompoundClasses:
				getTargetCompoundClassesSettings.validateSettings(settings);
				break;
			case GetTargetsByClass:
				getTargetsByClassesSettings.validateSettings(settings);
				break;
			case GetTargetPharmacology:
				getTargetPharmacologySettings.validateSettings(settings);
				break;
			case GetTargetPharmacologyByClass:
				getTargetPharmacologyByClassSettings.validateSettings(settings);
				break;
			case FreeTextSearch:
				freeTextSearchSettings.validateSettings(settings);
				break;
			default:
				break;
		}
	}
	
	public GetAssaySettings getAssaySettings() {
		return getAssaySettings;
	}
	
	public GetCompoundURISettings getCompoundURISettings() {
		return getCompoundURISettings;
	}
	
	public GetCompoundsBySimilaritySettings getCompoundsBySimilaritySettings() {
		return getCompoundsBySimilaritySettings;
	}
	
	public GetCompoundPharmacologySettings getCompoundPharmacologySettings() {
		return getCompoundPharmacologySettings;
	}
	
	public GetCompoundsBySubstructureSettings getCompoundsBySubstructureSettings() {
		return getCompoundsBySubstructureSettings;
	}

	public GetCompoundSettings getCompoundSettings() {
		return getCompoundSettings;
	}
	
	public GetCompoundClassesSettings getCompoundClassesSettings() {
		return getCompoundClassesSettings;
	}
	
	public GetCompoundsByClassSettings getCompoundsByClassSettings() {
		return getCompoundsByClassSettings;
	}
	
	public GetChildClassesSettings getChildClassesSettings() {
		return getChildClassesSettings;
	}
	
	public GetParentClassesSettings getParentClassesSettings() {
		return getParentClassesSettings;
	}

	public GetDiseaseSettings getDiseaseSettings() {
		return getDiseaseSettings;
	}
	
	public GetDiseaseAssociationsSettings getDiseaseAssociationsSettings() {
		return getDiseaseAssociationsSettings;
	}
	
	public GetTargetAssociationsSettings getTargetAssociationsSettings() {
		return getTargetAssociationsSettings;
	}

	public GetDiseasesByTargetSettings getDiseasesByTargetSettings() {
		return getDiseasesByTargetSettings;
	}
	
	public GetTargetsByDiseaseSettings getTargetsByDiseaseSettings() {
		return getTargetsByDiseaseSettings;
	}
	
	public GetTargetSettings getTargetSettings() {
		return getTargetSettings;
	}
	
	public GetTargetClassesSettings getTargetClassesSettings() {
		return getTargetClassesSettings;
	}
	
	public GetTargetCompoundClassesSettings getTargetCompoundClassesSettings() {
		return getTargetCompoundClassesSettings;
	}
	
	public GetTargetsByClassSettings getTargetsByClassSettings() {
		return getTargetsByClassesSettings;
	}
	
	public GetTargetPharmacologySettings getTargetPharmacologySettings() {
		return getTargetPharmacologySettings;
	}
	
	public GetTargetPharmacologyByClassSettings getTargetPharmacologyByClassSettings() {
		return getTargetPharmacologyByClassSettings;
	}
	
	public FreeTextSearchSettings getFreeTextSearchSettings() {
		return this.freeTextSearchSettings;
	}
	
	
	public boolean isAppend() {
		return bAppend;
	}
	
	public void setAppend(boolean b) {
		bAppend = b;
	}

	public ApplicationIdSetting getApplicationIdSetting() {
		return appId;
	}
	
	public ApplicationKeySetting getApplicationKeySetting() {
		return appKey;
	}
	
	public ReturnFormatTypeSetting getReturnFormatSetting() {
		return format;
	}
}


