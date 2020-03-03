/*
 * ------------------------------------------------------------------------
 *
 * Copyright (C) 2018 Eli Lilly and Company Limited
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
package org.erlwood.knime.utils.datacell;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataCellFactory.FromComplexString;
import org.knime.core.data.DataCellFactory.FromSimpleString;
import org.knime.core.data.DataType;
import org.knime.core.data.convert.DataCellFactoryMethod;
import org.knime.core.data.def.StringCell;

/** Class to create either a MissingCell or a StringCell based on the input value.
 * @author Tom Wilkin
 */
public class MissingOrStringCellFactory implements FromSimpleString, FromComplexString {
	
	/** Whether an empty String should be a missing value or not. */
	private boolean emptyIsMissing;
	
	/** Construct a new MissingOrStringCellFactory
	 * @param emptyIsMissing Whether an empty String should be a missing value or not.
	 */
	public MissingOrStringCellFactory(final boolean emptyIsMissing) {
		this.emptyIsMissing = emptyIsMissing;
	}

	/**
     * {@inheritDoc}
     */
	@DataCellFactoryMethod(name = "String")
	@Override
	public DataCell createCell(final String input) {
		return create(input, emptyIsMissing);
	}

	/**
     * {@inheritDoc}
     */
	@Override
	public DataType getDataType() {
		return StringCell.TYPE;
	}
	
	/** Create a DataCell with a MissingValue (if null, or empty) or a StringCell.
	 * @param input The input to create the cell for.
	 * @param emptyIsMissing Whether an empty String should be a missing value or not.
	 * @return The DataCell for the value. 
	 */
	public static DataCell create(final String input, final boolean emptyIsMissing) {
		if(input == null || (emptyIsMissing && "".equals(input))) {
			return DataType.getMissingCell();
		}
		
		return new StringCell(input);
	}
	
	/** Create a DataCell with a MissingValue (if null, or empty) or a StringCell.
	 * @param input The input to create the cell for.
	 * @return The DataCell for the value. 
	 */
	public static DataCell create(final String input) {
		return create(input, true);
	}

}
