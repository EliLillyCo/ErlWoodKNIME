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

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.HashSet;
import java.util.Set;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

/**
 * Settings class for holding values.
 * @author Luke Bullard
 *
 */
public abstract class AbstractSettings <M extends Enum<M>> {
	/**
	 * Method constant.
	 */
	protected static final String METHOD = "METHOD";

	/**
	 * The method to use.
	 */
	private M method;

	private final Class<M> typeOfT;
			
	/**
	 * Sets the method.
	 * @param m The new method.
	 */
	public void setMethod(M m) {
		this.method = m;		
	}
	
	/** 
	 * @return The method.
	 */
	public M getMethod() {
		return method;
	}
	
	@SuppressWarnings("unchecked")
	protected AbstractSettings() {
		this.typeOfT = (Class<M>)
                ((ParameterizedType)getClass()
                .getGenericSuperclass())
                .getActualTypeArguments()[0];
		
		M[] m = typeOfT.getEnumConstants();
		
		method = m[0];
	}
	
	/** Load the settings to this dialog tab.
	 * @param settings The settings to load the configuration from. 
	 * @throws InvalidSettingsException */	
	public void loadSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		checkSettingKeys( );
		
		M[] m = typeOfT.getEnumConstants();
		method = getMethod(settings, m[0].name());		
	}
	
	/**
	 * Gets the method enumeration value fro mthe settings.
	 * @param settings The settings object to read
	 * @param defaultName The default value to use if the setting is missing
	 * @return A valid enumeration value
	 * @throws InvalidSettingsException
	 */
	protected M getMethod(final NodeSettingsRO settings, String defaultName) throws InvalidSettingsException {
		M[] m = typeOfT.getEnumConstants();
		try {
			return M.valueOf(typeOfT, settings.getString(METHOD, defaultName));
		} catch (IllegalArgumentException ex) {
			return M.valueOf(typeOfT, defaultName);
		}
	}
	
	/** Save the settings of this dialog tab.
	 * @param settings The settings to store the configuration in. */
	public void saveSettingsTo(final NodeSettingsWO settings) {
		
		settings.addString(METHOD, method.name());
		
	}
	
	public void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		//	Do nothing by default.
	}

	public static boolean isDouble(String s) {
		if (s == null || s.length() == 0) {
			return false;
		}
		try {
			Double.parseDouble(s);
		} catch(NumberFormatException ex) {
			return false;
		}
		return true;
	}
	
	public static boolean isInteger(String s) {
		if (s == null || s.length() == 0) {
			return false;
		}
		try {
			Integer.parseInt(s);
		} catch(NumberFormatException ex) {
			return false;
		}
		return true;
	}
	
	/** Check the keys created for the AbstractSettings, and report if any are re-used.
	 * @throws InvalidSettingsException If one of the keys is re-used. */
	private void checkSettingKeys( ) throws InvalidSettingsException {
		// only execute when we're running as a test
		if("false".equals(System.getProperty("knime.test", "false"))) {
			return;
		}
		
		// check all the field keys for uniqueness
		Set<String> keys = new HashSet<String>( );
		String key;
		for(Field field : getClass( ).getDeclaredFields( )) {
			field.setAccessible(true);
			key = null;
			
			try {
				Class<?> clazz = field.getType( );
				if(IValueSettings.class.isAssignableFrom(clazz)) {
					key = ((IValueSettings)field.get(this)).getValueConfigKey( );
					if(keys.contains(key)) {
						throw new InvalidSettingsException("Duplicated value key '" + key + "'.");
					}
					keys.add(key);
				}
				
				if(IColumnNameSettings.class.isAssignableFrom(clazz)) {
					key = ((IColumnNameSettings)field.get(this)).getColumnConfigKey( );
					if(keys.contains(key)) {
						throw new InvalidSettingsException("Duplicated column key '" + key + "'.");
					}
					keys.add(key);
				}
			} catch(IllegalAccessException e) {
				// should not occur
			}
		}
	}
	
}



