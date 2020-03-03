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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.co.infocom.cheminfo.marvin.type.MrvCellFactory;
import jp.co.infocom.cheminfo.marvin.type.MrvValue;

import org.erlwood.knime.utils.chem.IMoleculeConverter;
import org.knime.chem.types.InchiCellFactory;
import org.knime.chem.types.InchiValue;
import org.knime.chem.types.MolCellFactory;
import org.knime.chem.types.MolValue;
import org.knime.chem.types.SdfCellFactory;
import org.knime.chem.types.SdfValue;
import org.knime.chem.types.SmartsCell;
import org.knime.chem.types.SmartsValue;
import org.knime.chem.types.SmilesCell;
import org.knime.chem.types.SmilesValue;
import org.knime.core.data.AdapterValue;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.DataValue;
import org.knime.core.data.StringValue;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.NodeLogger;

import chemaxon.formats.MFileFormat;
import chemaxon.formats.MolExporter;
import chemaxon.formats.MolImporter;
import chemaxon.struc.Molecule;

public class MarvinConverter implements IMoleculeConverter {
	private static final NodeLogger LOG = NodeLogger.getLogger(MarvinConverter.class);

	private static final Map<Class<? extends DataValue>, DataValueHolder> VALUE_TYPES = new HashMap<Class<? extends DataValue>, DataValueHolder>();
	private static final List<ConversionPair> AVAILABLE_CONVERSIONS;
	static {
		List<ConversionPair> tmpConversions = new ArrayList<ConversionPair>();
		
		try {
			DataValueHolder[] valueHolders = new DataValueHolder[] {
				new DataValueHolder(MolValue.class,		"getMolValue", 		MFileFormat.MOL),
			    new DataValueHolder(MrvValue.class,		"getMrvValue", 		MFileFormat.MRV),
			    new DataValueHolder(SmilesValue.class, 	"getSmilesValue", 	MFileFormat.SMILES),
			    new DataValueHolder(SdfValue.class, 	"getSdfValue", 		MFileFormat.SDF),
			    new DataValueHolder(InchiValue.class, 	"getInchiString", 	MFileFormat.INCHI),
			    new DataValueHolder(StringValue.class, 	"getStringValue", 	MFileFormat.MRV),
			    new DataValueHolder(SmartsValue.class, 	"getSmartsValue", 	MFileFormat.SMARTS),
			};
			
			for (DataValueHolder dvh : valueHolders) {
				VALUE_TYPES.put(dvh.valueType, dvh);
			}
			
			//	Now we construct the conversion matrix for all the conversion pairs
			for (int i = 0; i < valueHolders.length; i++) {
				for (int k = 0; k < valueHolders.length; k++) {
					final Class<? extends DataValue> input = valueHolders[i].valueType;
					final Class<? extends DataValue> output = valueHolders[k].valueType;
					
					//	Don't bother if the they are the same...
					if (input.equals(output)) {
						continue;
					}
					tmpConversions.add(new ConversionPair() {

						@Override
						public Class<? extends DataValue> getInput() {
							return input;
						}

						@Override
						public Class<? extends DataValue> getOuput() {
							return output;
						}
						
					});
				}
			}
			
		} catch(Exception ex) {
			LOG.error(ex.getMessage(), ex);
		}
		AVAILABLE_CONVERSIONS = Collections.unmodifiableList(tmpConversions);
	}
	
	@Override
	public List<ConversionPair> getAvailableConversions() {
		return AVAILABLE_CONVERSIONS;
	}

	@Override
	public DataCell createDataCell(DataCell source, Class<? extends DataValue> sourceType, Class<? extends DataValue> targetType) throws Exception {
		DataValue sv = null; //MoleculeDataTypeConverter.getValue(source, sourceType);
		DataType dt = source.getType();
		
		if (dt.isCompatible(sourceType)) {
			sv = source;
		} else 	if (dt.isAdaptable(sourceType)) {
			sv = ((AdapterValue)source).getAdapter(sourceType);
		}
		
		DataValueHolder inputDvh = VALUE_TYPES.get(sourceType);
		DataValueHolder outputDvh = VALUE_TYPES.get(targetType);
		return outputDvh.getCell(sv, inputDvh);
	}

	private static final class DataValueHolder {
		private final Class<? extends DataValue> valueType;
		private final MFileFormat marvinType;
		private final Method getter;
		
		public DataValueHolder(Class<? extends DataValue> valueType, String getterName, MFileFormat marvinType) throws Exception {
			this.valueType = valueType;
			this.marvinType = marvinType;
			this.getter = valueType.getMethod(getterName);
		}

		public Molecule getMolecule(DataValue input) throws Exception {
			return MolImporter.importMol((String)getter.invoke(input));
		}

		public Object getDataValue(Molecule mol) throws Exception {
			return MolExporter.exportToFormat(mol, marvinType.getName());
		}

		public DataCell getCell(DataValue sv, DataValueHolder inputDvh) throws Exception {
			Molecule mol = inputDvh.getMolecule(sv);
			Object obj = getDataValue(mol);
			
			if (valueType.equals(MolValue.class)) {
				return MolCellFactory.create((String) obj);
			}
			if (valueType.equals(MrvValue.class)) {
				return MrvCellFactory.create(mol);
			}
			if (valueType.equals(SmilesValue.class)) {
				return new SmilesCell((String) obj);
			}
			if (valueType.equals(SdfValue.class)) {
				return SdfCellFactory.create((String) obj);
			}
			if (valueType.equals(InchiValue.class)) {
				return InchiCellFactory.create((String) obj);
			}
			if (valueType.equals(StringValue.class)) {
				return new StringCell((String) obj);
			}
			if (valueType.equals(SmartsValue.class)) {
				if (sv instanceof StringValue && (!(sv instanceof MrvValue))) {
					return new SmartsCell(((StringValue) sv).getStringValue());
				} else {
					return new SmartsCell((String) obj);
				}
			}
			throw new IllegalStateException("Unable to create DatCell for " + valueType);
		}
		
	}

	@Override
	public String getName() {
		return "Marvin Converter";
	};
}
