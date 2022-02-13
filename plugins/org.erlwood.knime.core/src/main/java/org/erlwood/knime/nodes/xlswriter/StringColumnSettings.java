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

import java.awt.Color;

import org.apache.poi.hssf.util.HSSFColor;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;

/**
 * Adapted from class RenameColumnSetting written by Bernd Wiswedel, University
 * of Konstanz
 * 
 * @author Nikolas Fechner, Dimitar Hristozov
 */
public class StringColumnSettings {
	private static final NodeLogger LOGGER = NodeLogger
			.getLogger(StringColumnSettings.class);
	private static final NodeLogger LOG = NodeLogger.getLogger(StringColumnSettings.class);
	/** Config identifier for the original column name, used for sanity check. */
	public static final String CFG_OLD_COLNAME = "old_column_name";

	/** Config identifier for the new column name (if any). */
	public static final String CFG_QUERY = "query";
	public static final String CFG_QUERY2 = "query2";
	public static final String CFG_QUERY3 = "query3";

	public static final String CFG_COL1 = "col1";
	public static final String CFG_COL2 = "col2";
	public static final String CFG_COL3 = "col3";
	public static final String CFG_DCOL = "default";

	/**
	 * Config identifier for the index of the new type. The index is calculated
	 * on the order of the compatible types in constructPossibleTypes(DataType)
	 */
	public static final String CFG_SET = "set";
	public static final String CFG_INVERT = "invert";

	/** Original column name, never null. */
	private final String mName;

	/** New name of the column, may be null to retain the old name. */
	private String mQuery;
	private String mQuery2;
	private String mQuery3;

	private SettingsModelInteger mCol1 = new SettingsModelInteger(CFG_COL1,
			HSSFColor.HSSFColorPredefined.WHITE.getIndex());
	private SettingsModelInteger mCol2 = new SettingsModelInteger(CFG_COL2,
			HSSFColor.HSSFColorPredefined.WHITE.getIndex());
	private SettingsModelInteger mCol3 = new SettingsModelInteger(CFG_COL3,
			HSSFColor.HSSFColorPredefined.WHITE.getIndex());
	private SettingsModelInteger dCol = new SettingsModelInteger(CFG_DCOL,
			HSSFColor.HSSFColorPredefined.WHITE.getIndex());

	private boolean colourable = false;
	private boolean set = false;
	private boolean invert = false;

	public boolean isActive() {
		return set;
	}

	public boolean isInverted() {
		return invert;
	}

	/**
	 * Constructor being used in the NodeModel's configure method (for
	 * validation) and in the NodeDialog (DataTableSpec used to init default
	 * values like possible types).
	 * 
	 * @param column
	 *            the column spec form which to get values
	 */
	StringColumnSettings(final DataColumnSpec column) {
		mName = column.getName();
		colourable = isColourable(column.getType( ));
	}

	private StringColumnSettings(String name) {
		mName = name;
		colourable = true;
	}

	/**
	 * The name of the new column, if any. May be <code>null</code> when no new
	 * name was set.
	 * 
	 * @return the newColumnName
	 */
	String getQuery() {
		return mQuery;
	}

	String getQuery2() {
		return mQuery2;
	}

	String getQuery3() {
		return mQuery3;
	}

	/**
	 * Sets a new column name or <code>null</code>.
	 * 
	 * @param newColumnName
	 *            the newColumnName to set
	 */
	void setQuery(final String q) {
		mQuery = q;
		set = true;
	}

	void setQuery(int i, final String q) {
		switch (i) {
		case 1:
			mQuery = q;
			break;
		case 2:
			mQuery2 = q;
			break;
		case 3:
			mQuery3 = q;
			break;
		default:
			mQuery = q;
			break;
		}

		set = true;
	}

	/**
	 * @return the old name
	 */
	String getName() {
		return mName;
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
			LOGGER.warn("Can't safely update settings for column \"" + mName
					+ "\": No matching identifier.", ise);
			name = mName;
		}
		if (!mName.equals(name)) {
			LOGGER.warn("Can't update settings for column \"" + mName
					+ "\": got NodeSettings for \"" + name + "\"");
		}
		try {
			mQuery = settings.getString(CFG_QUERY);
			invert = settings.getBoolean(CFG_INVERT);
			try {
				mQuery2 = settings.getString(CFG_QUERY2);
				mQuery3 = settings.getString(CFG_QUERY3);

				mCol1.loadSettingsFrom(settings);
				mCol2.loadSettingsFrom(settings);
				mCol3.loadSettingsFrom(settings);
				dCol.loadSettingsFrom(settings);
			} catch (Exception e) {

				if (invert) {
					mCol1.setIntValue(HSSFColor.HSSFColorPredefined.RED.getIndex());
					mCol2.setIntValue(HSSFColor.HSSFColorPredefined.WHITE.getIndex());
					mCol3.setIntValue(HSSFColor.HSSFColorPredefined.WHITE.getIndex());
					dCol.setIntValue(HSSFColor.HSSFColorPredefined.WHITE.getIndex());
				} else {
					mCol1.setIntValue(HSSFColor.HSSFColorPredefined.GREEN.getIndex());
					mCol2.setIntValue(HSSFColor.HSSFColorPredefined.WHITE.getIndex());
					mCol3.setIntValue(HSSFColor.HSSFColorPredefined.WHITE.getIndex());
					dCol.setIntValue(HSSFColor.HSSFColorPredefined.WHITE.getIndex());
				}
			}

			set = settings.getBoolean(CFG_SET);
			colourable = isColourable(spec.getType( ));

		} catch (InvalidSettingsException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	static StringColumnSettings createFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		String name = settings.getString(CFG_OLD_COLNAME);
		String q = settings.getString(CFG_QUERY);
		String q2;
		try {
			q2 = settings.getString(CFG_QUERY2);
		} catch (Exception e) {
			q2 = "";
		}
		String q3;
		try {
			q3 = settings.getString(CFG_QUERY3);
		} catch (Exception e) {
			q3 = "";
		}
		boolean in = settings.getBoolean(CFG_INVERT);
		StringColumnSettings result = new StringColumnSettings(name);
		result.setQuery(1, q);
		result.setQuery(2, q2);
		result.setQuery(3, q3);
		result.set = settings.getBoolean(CFG_SET);
		result.invert = in;
		result.mCol1.setIntValue(settings.getInt(CFG_COL1));
		try {
			result.mCol2.setIntValue(settings.getInt(CFG_COL2));
			result.mCol3.setIntValue(settings.getInt(CFG_COL3));
			result.dCol.setIntValue(settings.getInt(CFG_DCOL));
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return result;
	}

	/**
	 * Save the current settings to a config.
	 * 
	 * @param settings
	 *            to save to
	 */
	void saveSettingsTo(final NodeSettingsWO settings) {
		settings.addString(CFG_OLD_COLNAME, mName);
		settings.addString(CFG_QUERY, mQuery);
		settings.addString(CFG_QUERY2, mQuery2);
		settings.addString(CFG_QUERY3, mQuery3);
		if (mQuery == null) {
			mQuery = "";
		}
		if (mQuery2 == null) {
			mQuery2 = "";
		}
		if (mQuery3 == null) {
			mQuery3 = "";
		}
		if (mQuery.isEmpty()) {
			mCol1.setIntValue(HSSFColor.HSSFColorPredefined.WHITE.getIndex());
		}
		if (mQuery2.isEmpty()) {
			mCol2.setIntValue(HSSFColor.HSSFColorPredefined.WHITE.getIndex());
		}
		if (mQuery3.isEmpty()) {
			mCol3.setIntValue(HSSFColor.HSSFColorPredefined.WHITE.getIndex());
		}
		mCol1.saveSettingsTo(settings);
		mCol2.saveSettingsTo(settings);
		mCol3.saveSettingsTo(settings);
		dCol.saveSettingsTo(settings);
		settings.addBoolean(CFG_SET, set);
		settings.addBoolean(CFG_INVERT, invert);
	}

	void setColorX(int i, Color c) {
		int cc = -1;
		if (c == Color.white) {
			cc = HSSFColor.HSSFColorPredefined.WHITE.getIndex();
		}
		if (c == Color.red) {
			cc = HSSFColor.HSSFColorPredefined.RED.getIndex();
		}
		if (c == Color.green) {
			cc = HSSFColor.HSSFColorPredefined.GREEN.getIndex();
		}
		if (c == Color.blue) {
			cc = HSSFColor.HSSFColorPredefined.BLUE.getIndex();
		}
		if (c == Color.yellow) {
			cc = HSSFColor.HSSFColorPredefined.YELLOW.getIndex();
		}

		switch (i) {
		case 1:
			mCol1.setIntValue(cc);
			break;
		case 2:
			mCol2.setIntValue(cc);
			break;
		case 3:
			mCol3.setIntValue(cc);
			break;
		case 4:
			dCol.setIntValue(cc);
			break;
		default:
			break;
		}
	}

	public int getColorX(int i) {
		switch (i) {
		case 1:
			return mCol1.getIntValue();
		case 2:
			return mCol2.getIntValue();
		case 3:
			return mCol3.getIntValue();
		case 4:
			return dCol.getIntValue();
		default:
			return -1;
		}
	}

	// white = 0, red =1, yellow =2, green =3, blue =4
	public int getColorIndex(int x) {
		if (x == HSSFColor.HSSFColorPredefined.WHITE.getIndex()) {
			return 0;
		}
		if (x == HSSFColor.HSSFColorPredefined.RED.getIndex()) {
			return 1;
		}
		if (x == HSSFColor.HSSFColorPredefined.YELLOW.getIndex()) {
			return 2;
		}
		if (x == HSSFColor.HSSFColorPredefined.GREEN.getIndex()) {
			return 3;
		}
		if (x == HSSFColor.HSSFColorPredefined.BLUE.getIndex()) {
			return 4;
		}
		return 0;
	}

	public boolean isColourable() {
		return colourable;
	}

	public boolean isSet() {
		return set;
	}

	public void setSet(boolean set) {
		this.set = set;
	}

	public boolean isInvert() {
		return invert;
	}

	public void setInvert(boolean invert) {
		this.invert = invert;
	}
	
	/** Return whether the column is colourable based on its type.
	 * @param type The type to check.
	 * @return Whether the specified type is colourable. */
	private boolean isColourable(final DataType type) {
		return type.isCompatible(StringCell.TYPE.getPreferredValueClass( ));
	}
	
}
