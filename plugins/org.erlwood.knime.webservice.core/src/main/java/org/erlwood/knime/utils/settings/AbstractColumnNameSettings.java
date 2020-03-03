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
package org.erlwood.knime.utils.settings;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;


/** Super class for the settings where a single column name can be selected.
 * @author Tom Wilkin */
public abstract class AbstractColumnNameSettings implements IColumnNameSettings {
	/**
	 * The column name.
	 */
	private String columnName;
	
	@Override
	public abstract String getColumnConfigKey();
	
	protected abstract String getValidationFailMessage();
	
	protected boolean isIgnoreValidation() {
		return false;
	}
	/**
	 * Sets the column name.
	 * @param n The new column name.
	 */
	public void setColumnName(String n) {
		this.columnName = n;
	}

	/**
	 * @return Gets the column name.
	 */
	public String getColumnName() {
		return columnName;
	}
	
	/** 
	 * Load the settings to this container.
	 * @param settings The settings to load the configuration from. 
	 * @throws InvalidSettingsException 
	 */
	public void loadSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		columnName 	= settings.getString(getColumnConfigKey(), 		null);
	}
	
	/** 
	 * Save the settings of this container.
	 * @param settings The settings to store the configuration in. 
	 */
	public void saveSettingsTo(final NodeSettingsWO settings) {
		settings.addString(getColumnConfigKey(), 		columnName);
	}

	/**
	 * Validate the settings.
	 * @param settings The settings to check
	 * @throws InvalidSettingsException On Error
	 */
	public void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {	
		String v = settings.getString(getColumnConfigKey(), null);
		if (!isIgnoreValidation()) {
			if (v == null || v.length() == 0) {
				throw new InvalidSettingsException(getValidationFailMessage());
			}
		}
	}
}
