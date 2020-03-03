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
package org.erlwood.knime.utils.chem;

import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataValue;

/**
 * Interface for supplying Molecule Converters.
 * @author Luke Bullard
 *
 */
public interface IMoleculeConverter {
	/**
	 * Holder for a valid conversion pairing.
	 * @author Luke Bullard
	 *
	 */
	public interface ConversionPair {
		Class<? extends DataValue> getInput();
		Class<? extends DataValue> getOuput();
	}
	
	/**
	 * @return A list of ConversionPair objects that this converter can handle.
	 */
	List<ConversionPair> getAvailableConversions();
	
	/**
	 * Creates a valid DataCell object of the given output type from the supplied source cell. 
	 * @param source The source DataCell to use.
	 * @param sourceType The type of the source.
	 * @param outputType The output type we require
	 * @return A valid DataCell
	 * @throws Exception On error.
	 */
	DataCell createDataCell(DataCell source,  Class<? extends DataValue> sourceType, Class<? extends DataValue> outputType) throws Exception;

	/**
	 * @return The name of the converter
	 */
	String getName();
	
	
}
