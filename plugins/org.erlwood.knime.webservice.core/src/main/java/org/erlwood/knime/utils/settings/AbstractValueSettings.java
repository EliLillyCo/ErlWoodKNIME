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

/**
 * Abstract class for value settings.
 * @author Luke Bullard
 *
 */
public abstract class AbstractValueSettings implements IValueSettings {
	
	/**
	 * The value.
	 */
	private String value;
	
	protected AbstractValueSettings() {
		value = getDefaultValue();
	}
	
	@Override
	public abstract String getValueConfigKey();

	protected abstract String getValidationFailMessage();
		
	protected boolean isIgnoreValidation() {
		return false;
	}
	
	public void loadSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {	
		value = settings.getString(getValueConfigKey(), getDefaultValue());	
	}
	
	protected String getDefaultValue() {
		return null;
	}
	
	public void saveSettingsTo(final NodeSettingsWO settings) {		
		settings.addString(getValueConfigKey(), value);		
	}
	
	/** 
	 * @return The value.
	 */
	public String getValue() {
		return value;
	}
	
	public double getDoubleValue() {
		return Double.parseDouble(getValue());
	}
	
	public int getIntegerValue() {
		return Integer.parseInt(getValue());
	}

	public boolean getBoolean() {	
		return Boolean.parseBoolean(getValue());
	}
	/**
	 * Sets the value.
	 * @param v The new value
	 */
	public void setValue(String v) {
		this.value = v;
	}
	
	/**
	 * Validate the settings.
	 * @param settings The settings to check
	 * @throws InvalidSettingsException On Error
	 */
	public void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {	
		String v = settings.getString(getValueConfigKey(), getDefaultValue());
		if (!isIgnoreValidation()) {
			if (v == null || v.length() == 0) {
				throw new InvalidSettingsException(getValidationFailMessage());
			}
			if (!validateValue(v)) {
				throw new InvalidSettingsException(getValidationFailMessage());
			}
		}
	}
	
	protected boolean validateValue(String v) {
		return true;
	}

	public static boolean validateDoubleValue(String v, double lower, double upper, boolean inclusive) {
		try {
			double d = Double.parseDouble(v);
			if (inclusive) {
				if (d < lower || d > upper) {
					return false;
				}
			} else {
				if (d <= lower || d >= upper) {
					return false;
				}
			}
		} catch(Exception ex) {
			return false;
		}
		return true;
	}
	
	public static boolean validateIntegerValue(String v, int lower, int upper, boolean inclusive) {
		try {
			int d = Integer.parseInt(v);
			if (inclusive) {
				if (d < lower || d > upper) {
					return false;
				}
			} else {
				if (d <= lower || d >= upper) {
					return false;
				}
			}
		} catch(Exception ex) {
			return false;
		}
		return true;
	}
}
