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
package org.erlwood.knime.nodes.uploadfileswithauth;

import org.erlwood.knime.utils.gui.auth.AuthenticationSettings;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/** Settings store for the Upload Files with Authentication node.
 * @author Tom Wilkin */
class UploadFilesWithAuthSettings extends AuthenticationSettings {
	
	/** Storage name for the share path option. */
	private final String SHARE_PATH_SETTING = "sharePath";
	
	/** Storage name for the path column option. */
	private final String PATH_COLUMN_SETTING = "pathCol";
	
	/** Storage name for the path variable option. */
	private final String PATH_VARIABLE_SETTING = "pathVar";
	
	/** Storage name for the path/column selected option. */
	private final String PATH_USE_COLUMN_SETTING = "pathUseColumn";
	
	/** Storage name for the overwrite option. */
	private final String OVERWRITE_SETTING = "overwrite";
	
	/** The path to the share to upload to. */
	private String sharePath;
	
	/** The name of the column containing the paths for the files to copy. */
	private String pathCol;
	
	/** The variable containing the path to copy. */
	private String pathVar;
	
	/** The variable storing whether the path is in use or not. */
	private boolean pathUseColumn;
	
	/** Whether the files at the destination should be overwritten. */
	private boolean overwrite;
	
	UploadFilesWithAuthSettings( ) {
		sharePath = "";
		pathCol = "";
		pathVar = "";
		pathUseColumn = true;
		overwrite = false;
	}
	
	/** @return The path to the share to upload to. */
	String getSharePath( ) {
		return sharePath;
	}
	
	/** Set the new value of the share path.
	 * @param sharePath The new value of the share path. */
	void setSharePath(final String sharePath) {
		this.sharePath = sharePath;
	}
	
	/** @return The name of the column containing the path of the files to copy. */
	String getPathColumn( ) {
		return pathCol;
	}
	
	/** Set the new value of the path column.
	 * @param pathCol The new value of the path column. */
	void setPathColumn(final String pathCol) {
		this.pathCol = pathCol;
	}
	
	/** The name of the variable containing the path to copy. */
	String getPathVariable( ) {
		return pathVar;
	}
	
	/** Set the new value of the path variable.
	 * @param pathVar The new value of the path variable. */
	void setPathVariable(final String pathVar) {
		this.pathVar = pathVar;
	}
	
	/** @return A SettingsModel for the path variable to use to create the DialogComponent. */
	SettingsModelString getPathVariableModel( ) {
		return new SettingsModelString(PATH_VARIABLE_SETTING, pathVar);
	}
	
	/** @return Whether to use the column (true) or variable (false) option. */
	boolean getPathUseColumn( ) {
		return pathUseColumn;
	}
	
	/** Set whether it is using the path column or variable.
	 * @param pathUseColumn Whether to use the path column or variable. */
	void setPathUseColumn(final boolean pathUseColumn) {
		this.pathUseColumn = pathUseColumn;
	}
	
	/** @return Whether the overwrite option is enabled. */
	boolean isOverwrite( ) {
		return overwrite;
	}
	
	/** Set whether to overwrite files or not.
	 * @param overwrite Whether to overwrite files. */
	void setOverwrite(final boolean overwrite) {
		this.overwrite = overwrite;
	}
	
	@Override
	public void saveSettingsTo(final NodeSettingsWO settings) {
		super.saveSettingsTo(settings);
		settings.addString(SHARE_PATH_SETTING, sharePath);
		settings.addString(PATH_COLUMN_SETTING, pathCol);
		settings.addString(PATH_VARIABLE_SETTING, pathVar);
		settings.addBoolean(OVERWRITE_SETTING, overwrite);
        settings.addBoolean(PATH_USE_COLUMN_SETTING, pathUseColumn);
	}
	
	@Override
	public void loadSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		super.loadSettingsFrom(settings);
		
		sharePath = settings.getString(SHARE_PATH_SETTING);
		pathCol = settings.getString(PATH_COLUMN_SETTING);
		pathVar = settings.getString(PATH_VARIABLE_SETTING);
		overwrite = settings.getBoolean(OVERWRITE_SETTING);
        
        try {
        	pathUseColumn = settings.getBoolean(PATH_USE_COLUMN_SETTING);
        } catch(InvalidSettingsException e) {
        	// new variable, work out what the value should be
        	if(pathCol != null && !"".equals(pathCol)) {
        		pathUseColumn = true;
        	} else if(pathVar != null && !"".equals(pathVar)) {
        		pathUseColumn = false;
        	} else {
        		// nothing was set, so enable the column selection by default
        		pathUseColumn = true;
        	}
        }
	}

}
