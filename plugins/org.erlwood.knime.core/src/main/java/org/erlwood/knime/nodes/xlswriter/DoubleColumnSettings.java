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
package org.erlwood.knime.nodes.xlswriter;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.vector.bitvector.DenseBitVectorCell;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

/**
 * Adapted from class RenameColumnSetting written by Bernd Wiswedel, University
 * of Konstanz
 * 
 * @author Nikolas Fechner, Dimitar Hristozov
 */
public class DoubleColumnSettings {
	private static final NodeLogger LOGGER = NodeLogger
			.getLogger(DoubleColumnSettings.class);

	/** Config identifier for the original column name, used for sanity check. */
	public static final String CFG_OLD_COLNAME = "old_column_name";

	/** Config identifier for the new column name (if any). */
	public static final String CFG_LOW_BOUND = "low_bound";

	/**
	 * Config identifier for the index of the new type. The index is calculated
	 * on the order of the compatible types in constructPossibleTypes(DataType)
	 */
	public static final String CFG_UP_BOUND = "up_bound";
	public static final String CFG_SET = "set";
	public static final String CFG_INVERT = "invert";

	/** Original column name, never null. */
	private final String objName;

	private double dLowB;
	private double dUpB;
	private boolean colourable = false;
	private boolean set = false;
	private boolean invert = false;

	public boolean isActive() {
		return set;
	}

	public boolean isInverted() {
		return invert;
	}

	public void setInverted(boolean newInverted) {
		this.invert = newInverted;
	}

	public boolean isColorable() {
		return colourable;
	}

	public void setColorable(boolean value) {
		this.colourable = value;
	}

	/**
	 * Constructor being used in the NodeModel's configure method (for
	 * validation) and in the NodeDialog (DataTableSpec used to init default
	 * values like possible types).
	 * 
	 * @param column
	 *            the column spec form which to get values
	 */
	DoubleColumnSettings(final DataColumnSpec column) {
		objName = column.getName();
		colourable = isColourable(column.getType( ));
	}

	private DoubleColumnSettings(String name) {
		objName = name;
		colourable = true;
	}

	/**
	 * The name of the new column, if any. May be <code>null</code> when no new
	 * name was set.
	 * 
	 * @return the newColumnName
	 */
	double getLowerBound() {
		return dLowB;
	}

	/**
	 * Sets a new column name or <code>null</code>.
	 * 
	 * @param newColumnName
	 *            the newColumnName to set
	 */
	void setLowerBound(final double low) {
		dLowB = low;
		set = true;
	}

	/**
	 * The name of the new column, if any. May be <code>null</code> when no new
	 * name was set.
	 * 
	 * @return the newColumnName
	 */
	double getUpperBound() {
		return dUpB;

	}

	/**
	 * Sets a new column name or <code>null</code>.
	 * 
	 * @param newColumnName
	 *            the newColumnName to set
	 */
	void setUpperBound(final double upper) {
		dUpB = upper;
		set = true;
	}

	/**
	 * @return the old name
	 */
	String getName() {
		return objName;
	}

	/**
	 * Loads settings from a settings object.
	 * 
	 * @param settings
	 *            to load from
	 */
	void loadSettingsFrom(final NodeSettingsRO settings, final DataColumnSpec spec) {
		String name;
		try {
			name = settings.getString(CFG_OLD_COLNAME);
		} catch (InvalidSettingsException ise) {
			// this method is called from the dialog which inits "this" first
			// and immediately calls this method, name should (must) match
			LOGGER.warn("Can't safely update settings for column \"" + objName
					+ "\": No matching identifier.", ise);
			name = objName;
		}
		if (!objName.equals(name)) {
			LOGGER.warn("Can't update settings for column \"" + objName
					+ "\": got NodeSettings for \"" + name + "\"");
		}
		try {
			dLowB = settings.getDouble(CFG_LOW_BOUND);
			dUpB = settings.getDouble(CFG_UP_BOUND);
			set = settings.getBoolean(CFG_SET);
			colourable = isColourable(spec.getType( ));
			invert = settings.getBoolean(CFG_INVERT);
		} catch (InvalidSettingsException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	static DoubleColumnSettings createFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		String name = settings.getString(CFG_OLD_COLNAME);
		double low = settings.getDouble(CFG_LOW_BOUND);
		double up = settings.getDouble(CFG_UP_BOUND);
		boolean in = settings.getBoolean(CFG_INVERT);
		DoubleColumnSettings result = new DoubleColumnSettings(name);
		result.setLowerBound(low);
		result.setUpperBound(up);
		result.set = settings.getBoolean(CFG_SET);
		result.invert = in;
		return result;
	}

	/**
	 * Save the current settings to a config.
	 * 
	 * @param settings
	 *            to save to
	 */
	void saveSettingsTo(final NodeSettingsWO settings) {
		settings.addString(CFG_OLD_COLNAME, objName);
		settings.addDouble(CFG_LOW_BOUND, dLowB);
		settings.addDouble(CFG_UP_BOUND, dUpB);
		settings.addBoolean(CFG_SET, set);
		settings.addBoolean(CFG_INVERT, invert);
	}

	public void setActive(boolean active) {
		this.set = active;

	}
	
	/** Return whether the column is colourable based on its type.
	 * @param type The type to check.
	 * @return Whether the specified type is colourable. */
	private boolean isColourable(final DataType type) {
		return type.isCompatible(DoubleCell.TYPE.getPreferredValueClass( ))
				|| type.isCompatible(IntCell.TYPE.getPreferredValueClass( ));
	}
	
}
