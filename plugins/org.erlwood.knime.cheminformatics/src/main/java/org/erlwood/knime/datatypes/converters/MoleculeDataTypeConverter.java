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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.erlwood.knime.utils.chem.AdapterDataValueFilter;
import org.erlwood.knime.utils.chem.IMoleculeConverter;
import org.erlwood.knime.utils.chem.IMoleculeConverter.ConversionPair;
import org.knime.chem.types.SdfValue;
import org.knime.core.data.AdapterValue;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.DataValue;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.util.ColumnFilter;
import org.osgi.framework.Bundle;

public final class MoleculeDataTypeConverter  {
	private static final NodeLogger LOG = NodeLogger.getLogger(MoleculeDataTypeConverter.class);
	
	private static final Map<Class<? extends DataValue>, Map<Class<? extends DataValue>, List<IMoleculeConverter>>> CONVERTER_MAP = new HashMap<Class<? extends DataValue>, Map<Class<? extends DataValue>, List<IMoleculeConverter>>>();
	private static final Map<Class<? extends DataValue>, Class<? extends DataValue>[]> CONVERTIBLE_TYPES = new ConcurrentHashMap<Class<? extends DataValue>, Class<? extends DataValue>[]>();
	private static final Map<String, List<ConverterChainEntry>> PREFERRED_CONVERTERS = new ConcurrentHashMap<String, List<ConverterChainEntry>>();
	
	
	static {
		IExtensionRegistry reg = Platform.getExtensionRegistry();

		//	Gets the Molecule Converters
		for (IConfigurationElement element : reg.getConfigurationElementsFor("org.erlwood.knime.cheminformatics.moleculeconverters")) {
			
			IMoleculeConverter converter = null;
			
			try {
				converter = (IMoleculeConverter) element.createExecutableExtension("class");
				for (ConversionPair convPair : converter.getAvailableConversions()) {
					
					//	Check to see if we already have a list of converters for this output type
					Map<Class<? extends DataValue>, List<IMoleculeConverter>> map = CONVERTER_MAP.get(convPair.getOuput());
					if (map == null) {
						map = new HashMap<Class<? extends DataValue>, List<IMoleculeConverter>>();
						CONVERTER_MAP.put(convPair.getOuput(), map);
					}
					List<IMoleculeConverter> converters = map.get(convPair.getInput());
					if (converters == null) {
						converters = new ArrayList<IMoleculeConverter>();
						map.put(convPair.getInput(), converters);
					}
					converters.add(converter);
				}
				
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
			
		}
		
		
		
		//	Gets the preferred converter mappings
		for (IConfigurationElement element : reg.getConfigurationElementsFor("org.erlwood.knime.cheminformatics.preferredconverters")) {
						
			try {
				Bundle bundle = Platform.getBundle(element.getNamespaceIdentifier());
				
				Class<? extends DataValue> input = (Class<? extends DataValue>)bundle.loadClass(element.getAttribute("input"));
				Class<? extends DataValue> output = (Class<? extends DataValue>)bundle.loadClass(element.getAttribute("output"));
				
				IMoleculeConverter converter = (IMoleculeConverter)element.createExecutableExtension("converter");
				String key = createConverterKey(output, input);
				//PREFERRED_CONVERTERS.put(key, converter);
				
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
			
		}
	}
	
	/**
	 * Private constructor - utility class.
	 */
	private MoleculeDataTypeConverter() {
	}
	
	/**
	 * Checks to see if the supplied DataType is either compatible or adaptable to the supplied DataValue given
	 * access to an appropriate converter. 
	 * @param type The DataType to check
	 * @param typeToCheck The desired DataValue
	 * @return TRUE if the DataType is  either compatible or adaptable to the supplied DataValue
	 */
	public static boolean isConvertible(DataType type, Class<? extends DataValue> typeToCheck) {
		for (Class<? extends DataValue> dv : getConvertibleTypes(typeToCheck)) {
			if (type.isCompatible(dv)) {
				return true;
			}
			if (type.isAdaptable(dv)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks to see if the supplied DataType is either compatible or adaptable to the supplied DataValue without regard to conversions.
	 * @param type The DataType to check
	 * @param typeToCheck The desired DataValue
	 * @return TRUE if the DataType is  either compatible or adaptable to the supplied DataValue
	 */
	public static boolean isCompatible(DataType type, Class<? extends DataValue> typeToCheck) {
		
		if (type.isCompatible(typeToCheck)) {
			return true;
		}
		if (type.isAdaptable(typeToCheck)) {
			return true;
		}
		
		return false;
	}
	
	/**
     * Checks to see if the supplied DataType is either compatible or adaptable to the supplied DataValue without regard to conversions.
     * @param type The DataType to check
     * @param typesToCheck The desired DataValues
     * @return TRUE if the DataType is  either compatible or adaptable to the supplied DataValue
     */
    public static boolean isCompatible(DataType type, Class<? extends DataValue>... typesToCheck) {
        
        for (Class<? extends DataValue> t : typesToCheck) {
            if (isCompatible(type, t)) {
                return true;
            }
        }
        
        return false;
    }
	
	/**
	 * Convenience method to return a list of available DataValue conversions based on the registered
	 * IMoleculeConvter contributions.
	 * @param outputType The desired output DataValue
	 * @return An array of DataVaue classes that can be converted to the outputType.
	 */
	public static Class<? extends DataValue>[] getConvertibleTypes(Class<? extends DataValue> outputType) {
		Class<? extends DataValue>[] retVal = null;
		retVal = CONVERTIBLE_TYPES.get(outputType);
		if (retVal != null) {
			return retVal;
		}
		
		Set<Class<? extends DataValue>> typeSet = new HashSet<Class<? extends DataValue>>();
		typeSet.add(outputType);
		
		boolean bCanUseSdf = false;
		
		Map<Class<? extends DataValue>, List<IMoleculeConverter>> map = CONVERTER_MAP.get(outputType);
		if (map != null) {
			for (Class<? extends DataValue> cnv : map.keySet()) {
				if (typeSet.contains(cnv)) {
					continue;
				}
				typeSet.add(cnv);
				if (cnv.isAssignableFrom(SdfValue.class)) {
					bCanUseSdf = true;
				}
			}
		}
		
		if (bCanUseSdf) {
			//	Now add those types that can convert to Sdf...
			map = CONVERTER_MAP.get(SdfValue.class);
			if (map != null) {
				for (Class<? extends DataValue> cnv : map.keySet()) {
					if (typeSet.contains(cnv)) {
						continue;
					}
					typeSet.add(cnv);
				}
			}
		}
		retVal = (Class<? extends DataValue>[]) typeSet.toArray(new Class<?>[0]);
		CONVERTIBLE_TYPES.put(outputType, retVal);
		return retVal;
	}
	
	/**
	 * Takes the required output type and the source DataCell and down-calls into the appropriate
	 * converter object. 
	 * @param outputType The required output DataValue
	 * @param source The source cell
	 * @return A valid DataCell
	 * @throws Exception On error
	 */
	private static DataCell createDataCell(Class<? extends DataValue> outputType, DataCell source) throws Exception {
		DataType dt = source.getType();
		
		if (dt.isCompatible(outputType)) {
			return source;
		}
		if (dt.isAdaptable(outputType)) {
			return source;
		}
		
		
		List<ConverterChainEntry> preferredConverterChain = getPreferredConverterChain(outputType, dt);
		
		DataCell outputCell = null;
		DataCell tmpCell = source;
		for (ConverterChainEntry conv : preferredConverterChain) {
			outputCell = conv.createDataCell(tmpCell);
			tmpCell = outputCell;
		}
		return outputCell;
	}
	
	/**
	 * Internal convenience method to create the concatenated preferred converter key.
	 * @param outputType The output DataValue
	 * @param inputType The input DataValue
	 * @return A string key.
	 */
	private static String createConverterKey(Class<? extends DataValue> outputType, Class<? extends DataValue> inputType) {
		return inputType.getName() + ":" + outputType.getName();
	}

	/**
	 * Either returns a previously selected preferred converter for the input/output types or works out which converter should be preferred and
	 * then stores and returns it.
	 * @param outputType The output DataValue
	 * @param dt The input DataType
	 * @return A valid IMoleculeConverter chain
	 * @throws Exception On error
	 */
	private static List<ConverterChainEntry> getPreferredConverterChain(Class<? extends DataValue> outputType, DataType dt) throws Exception {
		List<ConverterChainEntry> preferredConverterChain = null;
		
		preferredConverterChain = getConverterChain(outputType, dt.getPreferredValueClass());
        if (preferredConverterChain.size() > 0) {
            return preferredConverterChain;
        }
        
		for (Class<? extends DataValue> dv : dt.getValueClasses()) {
		    preferredConverterChain = getConverterChain(outputType, dv);
		    if (preferredConverterChain.size() > 0) {
		        return preferredConverterChain;
		    }
		}
		
		return null;
	}
	
	/**
     * Either returns a previously selected preferred converter for the input/output types or works out which converter should be preferred and
     * then stores and returns it.
     * @param outputType The output DataValue
     * @param inputType The input DataValue
     * @return A valid IMoleculeConverter chain
     * @throws Exception On error
     */
    private static List<ConverterChainEntry> getConverterChain(Class<? extends DataValue> outputType, Class<? extends DataValue> inputType) throws Exception {
        List<ConverterChainEntry> preferredConverterChain = null;
      
        String key = createConverterKey(outputType, inputType);
        
        preferredConverterChain = PREFERRED_CONVERTERS.get(key);
        if (preferredConverterChain != null) {
            return preferredConverterChain;
        }
        preferredConverterChain = findConversionRoute(outputType, inputType);
        
        PREFERRED_CONVERTERS.put(key, preferredConverterChain);
        return preferredConverterChain;

    }
	
	public static ColumnFilter getColumnFilter(Class<? extends DataValue> valueType) {
		return new AdapterDataValueFilter(getConvertibleTypes(valueType));
	}
	
	public static String getPreferredConversionRouteText(DataType dataType,  Class<? extends DataValue> targetType) {
		StringBuffer sb  = new StringBuffer();
		for (Class<? extends DataValue> vl : dataType.getValueClasses()) {
    		List<ConverterChainEntry> lst = findConversionRoute(targetType, vl);
    		if (lst == null || lst.size() == 0) {
    		    continue;
    		}
    		for (ConverterChainEntry e : lst) {
    			 sb.append(e.getConverter().getName() + " / ");
    		}
    		sb.setLength(sb.length() - 3);
    		break;
		}
		return sb.toString();
	}
	
	private static List<ConverterChainEntry> findConversionRoute(Class<? extends DataValue> outputType,  Class<? extends DataValue> inputType) {
		Set<Class<? extends DataValue>> scannedTypes = new HashSet<Class<? extends DataValue>>();
		return findConversionRoute(outputType, inputType, scannedTypes);
	}
	
	private static List<ConverterChainEntry> findConversionRoute(Class<? extends DataValue> outputType,  Class<? extends DataValue> inputType, Set<Class<? extends DataValue>> scannedTypes) {
		List<ConverterChainEntry> retVal = new ArrayList<ConverterChainEntry>();
	
		ConverterChainEntry step1 = null;
		
		Map<Class<? extends DataValue>, List<IMoleculeConverter>> map = CONVERTER_MAP.get(outputType);
		if (map != null) {
			for (Entry<Class<? extends DataValue>, List<IMoleculeConverter>>  es : map.entrySet()) {
				if (es.getKey().equals(inputType)) {
					retVal.add(new ConverterChainEntry(es.getValue().get(0), es.getKey(), outputType));
					return retVal;
				} else {
					if (es.getKey().isAssignableFrom(SdfValue.class)) {
						step1 = new ConverterChainEntry(es.getValue().get(0), es.getKey(), outputType);
					}
				}
			}
		}
		
		//	Must go through SDF...
		if (step1 != null) {
			map = CONVERTER_MAP.get(SdfValue.class);
			if (map != null) {
				for (Entry<Class<? extends DataValue>, List<IMoleculeConverter>>  es : map.entrySet()) {
					if (es.getKey().isAssignableFrom(inputType)) {
						retVal.add(new ConverterChainEntry(es.getValue().get(0), inputType, SdfValue.class));
						retVal.add(step1);
						break;
					}
				}
			}
		}
	
		return retVal;
		
	}
	
	/**
	 * Convenience method to extract a DataValue object from a cell irrespective of whether
	 * it's a normal implementation or an adapter cell.
	 * @param source The cell to extract the value from
	 * @param outputType The type of the output that is required
	 * @return A DataValue of the correct type.
	 */
	@SuppressWarnings("unchecked")
	public static <V extends DataValue> V getValue(DataCell source, Class<V> outputType) {
		DataCell newSource = null;
		
		try {
			newSource = createDataCell(outputType, source);
			
			DataType dt = newSource.getType();
			if (dt.isCompatible(outputType)) {
				return (V)newSource;
			}
			if (dt.isAdaptable(outputType)) {
				return ((AdapterValue)newSource).getAdapter(outputType);
			}
		} catch(Exception ex) {
			LOG.debug(ex.getMessage(), ex);
		}
		
		throw new IllegalStateException("Source Cell cannot be converted to " + outputType.getSimpleName());
	}

	
	private static final class ConverterChainEntry {
		private final IMoleculeConverter converter;
		private final Class<? extends DataValue> inputType;
		private final Class<? extends DataValue> outputType;
		
		public ConverterChainEntry(IMoleculeConverter conv,
									Class<? extends DataValue> inputType,
									Class<? extends DataValue> outputType) {
			this.converter = conv;
			this.inputType = inputType;
			this.outputType = outputType;
		}

		public IMoleculeConverter getConverter() {
			return converter;
		}

		public DataCell createDataCell(DataCell source) throws Exception {
			return converter.createDataCell(source, inputType, outputType);
		}
	}


}
