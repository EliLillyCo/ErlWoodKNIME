/*
 * ------------------------------------------------------------------------
 *
 * Copyright (C) 2015 Eli Lilly and Company Limited
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
package org.erlwood.knime.nodes.downloadfileswithauth;

import org.erlwood.knime.utils.gui.auth.AuthenticationSettings;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/** Settings store for the Download Files with Authentication node.
 * @author Tom Wilkin */
class DownloadFilesWithAuthSettings extends AuthenticationSettings {
	
	/** Storage name for the URL column option. */
	private final String URL_COLUMN_SETTING = "urlCol";
	
	/** Storage name for the delete temporary directory option. */
	private final String DELETE_TEMP_SETTING = "deleteTemp";
	
	/** Storage name for the output directory enabled option. */
	private final String OUTPUT_DIRECTORY_ENABLED_SETTING = "outputDirectoryEnabled";
	
	/** Storage name for the output directory option. */
	private final String OUTPUT_DIRECTORY_SETTING = "outputDirectory";
	
	/** The column containing the URLs of the files to download. */
	private SettingsModelString urlCol;
	
	/** The option specifying whether the temporary files should be deleted on reset. */
	private SettingsModelBoolean deleteTemp;
	
	/** Whether a custom output directory is defined. */
	private SettingsModelBoolean outputDirectoryEnabled;
	
	/** The custom output directory to download the files to. */
	private SettingsModelString outputDirectory;
	
	DownloadFilesWithAuthSettings( ) {
		urlCol = new SettingsModelString(URL_COLUMN_SETTING, "");
		deleteTemp = new SettingsModelBoolean(DELETE_TEMP_SETTING, true);
		outputDirectoryEnabled = new SettingsModelBoolean(OUTPUT_DIRECTORY_ENABLED_SETTING, false);
		outputDirectory = new SettingsModelString(OUTPUT_DIRECTORY_SETTING, "");
	}
	
	/** @return The Settings Model for the URL column. */
	SettingsModelString getUrlColumnModel( ) {
		return urlCol;
	}
	
	/** @return The name of the column containing the URLs to download. */
	String getUrlColumn( ) {
		return urlCol.getStringValue( );
	}
	
	/** @return The Settings Model for the delete temporary directory option. */
	SettingsModelBoolean getDeleteTempModel( ) {
		return deleteTemp;
	}
	
	/** @return Whether the temporary directory should be deleted on reset. */
	boolean isDeleteTemp( ) {
		return deleteTemp.getBooleanValue( );
	}
	
	/** @return The Settings Model for the custom output directory enabled option. */
	SettingsModelBoolean getOutputDirectoryEnabledModel( ) {
		return outputDirectoryEnabled;
	}
	
	/** @return Whether the custom output directory is enabled. */
	boolean isOutputDirectoryEnabled( ) {
		return outputDirectoryEnabled.getBooleanValue( );
	}
	
	/** @return The Settings Model for the custom output directory path. */
	SettingsModelString getOutputDirectoryModel( ) {
		return outputDirectory;
	}
	
	/** @return The path to the custom output directory to download the files to. */
	String getOutputDirectory( ) {
		return outputDirectory.getStringValue( );
	}
	
	@Override
	public void saveSettingsTo(final NodeSettingsWO settings) {
		urlCol.saveSettingsTo(settings);
		deleteTemp.saveSettingsTo(settings);
		outputDirectoryEnabled.saveSettingsTo(settings);
		outputDirectory.saveSettingsTo(settings);
		
		super.saveSettingsTo(settings);
	}
	
	@Override
	public void loadSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		urlCol.loadSettingsFrom(settings);
		deleteTemp.loadSettingsFrom(settings);
		
		try {
			outputDirectoryEnabled.loadSettingsFrom(settings);
			outputDirectory.loadSettingsFrom(settings);
		} catch(InvalidSettingsException e) {
			// this must be an old workflow without these settings, so use defaults
			outputDirectoryEnabled.setBooleanValue(false);
			outputDirectory.setStringValue("");
		}
		
		super.loadSettingsFrom(settings);
	}
	
	/** Validate the settings for each of the stored settings.
	 * @param settings The settings to check the values of.
	 * @throws InvalidSettingsException If any of the settings are invalid. */
	public void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		urlCol.validateSettings(settings);
		deleteTemp.validateSettings(settings);
		
		try {
			outputDirectoryEnabled.validateSettings(settings);
			outputDirectory.validateSettings(settings);
		} catch(InvalidSettingsException e) {
			// ignore as this is an old workflow without this settings
		}
	}

}
