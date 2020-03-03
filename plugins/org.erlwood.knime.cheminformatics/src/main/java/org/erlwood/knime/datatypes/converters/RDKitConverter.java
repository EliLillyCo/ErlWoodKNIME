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
package org.erlwood.knime.datatypes.converters;

import java.util.ArrayList;
import java.util.List;

import org.knime.chem.types.SdfCell;
import org.knime.chem.types.SdfValue;
import org.knime.chem.types.SmilesCell;
import org.knime.chem.types.SmilesValue;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.DataValue;
import org.knime.core.node.NodeLogger;
import org.erlwood.knime.utils.chem.IMoleculeConverter;
import org.rdkit.knime.types.RDKitMolValue;
import org.rdkit.knime.util.RDKitAdapterCellSupport;

public class RDKitConverter implements IMoleculeConverter {
	private static final NodeLogger LOG = NodeLogger.getLogger(RDKitConverter.class);

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ConversionPair> getAvailableConversions() {
		List<ConversionPair> lst = new ArrayList<ConversionPair>();
		
		lst.add(new ConversionPair() {
			
			@Override
			public Class<? extends DataValue> getOuput() { return RDKitMolValue.class; }
			
			@Override
			public Class<? extends DataValue> getInput() { return SdfValue.class; 	}
		});
		
		lst.add(new ConversionPair() {
			
			@Override
			public Class<? extends DataValue> getOuput() { 	return RDKitMolValue.class; }
			
			@Override
			public Class<? extends DataValue> getInput() { 	return SmilesValue.class; 		}
		});
		return lst;	
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataCell createDataCell(DataCell source,
			Class<? extends DataValue> sourceType,
			Class<? extends DataValue> outputType) throws Exception {
		
			
		if (sourceType.isAssignableFrom(SmilesValue.class)) {
			return RDKitAdapterCellSupport.createConverter(SmilesCell.TYPE, RDKitMolValue.class).convert(source);
		}
		
		if (sourceType.isAssignableFrom(SdfValue.class)) {
			return RDKitAdapterCellSupport.createConverter(SdfCell.TYPE, RDKitMolValue.class).convert(source);
		}
		
		return DataType.getMissingCell();
	}

	@Override
	public String getName() {
		return "RDKit Converter";
	};
}
