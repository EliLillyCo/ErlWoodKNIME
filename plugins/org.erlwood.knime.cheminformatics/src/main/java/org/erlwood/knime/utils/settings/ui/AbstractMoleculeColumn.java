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
package org.erlwood.knime.utils.settings.ui;

import java.util.Map;
import java.util.TreeMap;

import org.erlwood.knime.datatypes.converters.MoleculeDataTypeConverter;
import org.erlwood.knime.utils.gui.ConverterDataColumnSpecListCellRenderer;
import org.erlwood.knime.utils.settings.AbstractColumnNameSettings;
import org.erlwood.knime.utils.settings.AbstractSettings;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataValue;
import org.knime.core.node.port.PortObjectSpec;

/**
 * Panel used to configure the a webservice call.
 * @author Luke Bullard
 *
 */
public abstract class AbstractMoleculeColumn <M extends Enum<M>, T extends AbstractColumnNameSettings, V extends AbstractSettings<M>> extends AbstractColumn<M,T,V> {
	/**
	 * Serial Id.
	 */
	private static final long serialVersionUID = 1L;
			
	/**
	 * Constructor.
	 */
	public AbstractMoleculeColumn() {			
		ConverterDataColumnSpecListCellRenderer.setRenderer(getComboBox(), getDefaultDataValue());				
	}
	
	/**
	 * Extracts the DataColumnSpec values from the first element of a PortObjectSpec[] that is assumed to actually be
	 * a DataTableSpec.
	 * @param specs The specs
	 * @param typesToCheck The types to use to extract the DataColumnSpec for
	 * @return A Map object with the name of the DataColumnSpec as the key.
	 */
	@Override
	protected Map<String, DataColumnSpec> getColumnSpecMap(final PortObjectSpec[] specs, Class<? extends DataValue>... typesToCheck) {
		
    	Map<String, DataColumnSpec> columnMap = new TreeMap<String, DataColumnSpec>();
    	    	
    	if (specs == null || getInputPortNumber()  > specs.length - 1 || specs[getInputPortNumber()] == null) {
    		return columnMap;
    	}
    	for (DataColumnSpec dcs : (DataTableSpec) specs[getInputPortNumber()]) {
    		for (Class<? extends DataValue> dt : typesToCheck) {
				if (MoleculeDataTypeConverter.isConvertible(dcs.getType(), dt)) {
					columnMap.put(dcs.getName(), dcs);
				}
    		} 
    	}
    	return columnMap;
	}

}
