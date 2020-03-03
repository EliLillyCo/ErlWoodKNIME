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

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataValue;
import org.knime.core.node.port.PortObjectSpec;

/**
 * Abstract class for ui settings. 
 * @author Luke Bullard
 *
 */
@SuppressWarnings("serial")
public abstract class AbstractSettingsPanel extends JPanel  {
	
	protected int getInputPortNumber() {
		return 0;
	}
	
	/**
	 * Extracts the DataColumnSpec values from the first element of a PortObjectSpec[] that is assumed to actually be
	 * a DataTableSpec.
	 * @param specs The specs
	 * @param typesToCheck The types to use to extract the DataColumnSpec for
	 * @return A Map object with the name of the DataColumnSpec as the key.
	 */
	protected Map<String, DataColumnSpec> getColumnSpecMap(final PortObjectSpec[] specs, Class<? extends DataValue>... typesToCheck) {
		Map<String, DataColumnSpec> columnMap = new TreeMap<String, DataColumnSpec>();
    	
    	if (specs == null || getInputPortNumber()  > specs.length - 1 || specs[getInputPortNumber()] == null) {
    		return columnMap;
    	}
    	for (DataColumnSpec dcs : (DataTableSpec) specs[getInputPortNumber()]) {    					
    		columnMap.put(dcs.getName(), dcs);				    		 
    	}
    	return columnMap;
	}

		
	/**
	 * Gets the name of the selected DataColumnSpec from a JComboBox
	 * @param cbo The JComboBox to examine.
	 * @return The name of the selected DataColumnSpec
	 */
	protected String getComboName(JComboBox<DataColumnSpec> cbo) {
        if (cbo.getSelectedItem() == null) {
            return null;
        }
        return ((DataColumnSpec)cbo.getSelectedItem()).getName();
    }
	
	/**
	 * {@inheritDoc}
	 */
	public Component getPanel() {
		return this;
	}
	
	protected ItemListener getSelectionItemListener (CardLayout cl , JPanel stack) {
		
		return new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					cl.show(stack, ((Enum) e.getItem()).name());
				}
			}
		};
		
	}
}
