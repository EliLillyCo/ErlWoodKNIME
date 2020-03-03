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
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

/**
 * Abstract class for column or single value settings.
 * @author Luke Bullard
 *
 */
public abstract class AbstractColumnAndValueSettings 
		implements IColumnNameSettings, IValueSettings 
{
	
	/**
	 * The use column.
	 */
	private boolean useColumn;
	
	private AbstractColumnNameSettings col = new AbstractColumnNameSettings() {

		@Override
		public String getColumnConfigKey() {
			return AbstractColumnAndValueSettings.this.getColumnConfigKey();
		}

		@Override
		protected String getValidationFailMessage() {
			return getColumnValidationFailMessage();
		}
		
	};
	
	private AbstractValueSettings value = new AbstractValueSettings() {

		@Override
		public String getValueConfigKey() {
			return AbstractColumnAndValueSettings.this.getValueConfigKey();
		}

		@Override
		protected String getValidationFailMessage() {
			return getValueValidationFailMessage();
		}

		@Override
		protected boolean validateValue(final String v) {
			return AbstractColumnAndValueSettings.this.validateValue(v);
		}
		
	};
	
	@Override
	public abstract String getValueConfigKey();
	
	@Override
	public abstract String getColumnConfigKey();
	
	protected abstract String getUseColumnConfigKey();	
	protected abstract String getColumnValidationFailMessage();
	protected abstract String getValueValidationFailMessage();
	
	protected boolean isIgnoreValidation() {
		return false;
	}
	
	protected boolean useColumnByDefault() {
		return false;
	}
	
	public void loadSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		loadSettingsFrom(settings, false);
	}
	
	/** 
	 * Load the settings.
	 * @param settings The settings instance to load from.
	 * @param isNew Whether to ignore the InvalidSettingsException for new fields.
	 * @throws InvalidSettingsException If an error occurs loading the settings.
	 */
	public void loadSettingsFrom(final NodeSettingsRO settings, final boolean isNew) throws InvalidSettingsException {
		try {
			col.loadSettingsFrom(settings);
			value.loadSettingsFrom(settings);
			useColumn = settings.getBoolean(getUseColumnConfigKey(), 	useColumnByDefault());
		} catch(InvalidSettingsException e) {
			if(isNew) {
				// if this is a new field, we just want to ignore the exception for backwards compatibility
				NodeLogger.getLogger(this.getClass()).error(e);
			} else {
				// if this field has always been set, we want to throw the error
				throw e;
			}
		}
	}
		
	public void saveSettingsTo(final NodeSettingsWO settings) {
		col.saveSettingsTo(settings);
		value.saveSettingsTo(settings);
		settings.addBoolean(getUseColumnConfigKey(),	useColumn);
	}
	
	/** 
	 * @return The value.
	 */
	public String getValue() {
		return value.getValue();
	}

	/**
	 * Sets the value.
	 * @param v The new value
	 */
	public void setValue(String v) {
		this.value.setValue(v);
	}

	/** 
	 * @return Are we using the column name ?
	 */
	public boolean isUseColumn() {
		return useColumn;
	}
	
	/**
	 * Sets whether we are using the column ? 
	 * @param useColumn The new value.
	 */
	public void setUseColumn(boolean useColumn) {
		this.useColumn = useColumn;
	}
	
	/**
	 * Sets the column name.
	 * @param n The new column name.
	 */
	public void setColumnName(String n) {
		this.col.setColumnName(n);
	}

	/**
	 * @return Gets the column name.
	 */
	public String getColumnName() {
		return col.getColumnName();
	}
	
	/**
	 * Validate the settings.
	 * @param settings The settings to check
	 * @throws InvalidSettingsException On Error
	 */
	public void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {	
				
		if (settings.getBoolean(getUseColumnConfigKey(), false)) {
			col.validateSettings(settings);
		} else {
			validateValueSettings(settings);
		}
	}
	
	/** Allow the validateValue method of the internal value to be overridden by extending classes.
	 * Validate that that supplied value is within the allowable range of values.
	 * @param v The value to check.
	 * @return Whether the value is within the allowable range. */
	protected boolean validateValue(final String v) {
		return true;
	}
	
	/** Allow the validateValueSettings method of the internal value to be overridden by extending
	 * classes.
	 * @param settings The settings to validate.
	 * @throws InvalidSettingsException If the settings are not valid. */
	protected void validateValueSettings(final NodeSettingsRO settings) 
			throws InvalidSettingsException
	{
		if (!isIgnoreValidation()) {
			value.validateSettings(settings);
		}
	}

}
