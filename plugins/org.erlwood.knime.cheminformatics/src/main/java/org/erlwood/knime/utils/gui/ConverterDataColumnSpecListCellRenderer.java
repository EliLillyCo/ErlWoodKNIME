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
package org.erlwood.knime.utils.gui;

import java.awt.Component;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;

import org.erlwood.knime.datatypes.converters.MoleculeDataTypeConverter;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataValue;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.util.DataColumnSpecListCellRenderer;


@SuppressWarnings("serial")
public class ConverterDataColumnSpecListCellRenderer extends DataColumnSpecListCellRenderer {
	private final Class<? extends DataValue> targetType;

	public ConverterDataColumnSpecListCellRenderer(Class<? extends DataValue> targetType) {
		this.targetType = targetType;
	}
	
	/**
     * {@inheritDoc}
     */
    @Override
    public Component getListCellRendererComponent(
            final JList list, final Object value, final int index,
            final boolean isSelected, final boolean cellHasFocus) {
    	
    	DataColumnSpec spec = (DataColumnSpec)value;
    	if (spec != null && !MoleculeDataTypeConverter.isCompatible(spec.getType(), targetType)) {
    		DataColumnSpecCreator sc = new DataColumnSpecCreator(spec);
    		String s = MoleculeDataTypeConverter.getPreferredConversionRouteText(spec.getType(), targetType);
    		
    		sc.setName(spec.getName() + ((s == null || s.length() == 0) ? "" : " (via " + s + ")"));
    		spec = sc.createSpec();
    	}
    	
        return super.getListCellRendererComponent(list, spec, index, isSelected, cellHasFocus);
    }

    public static boolean setRenderer(DialogComponentColumnNameSelection c, Class<? extends DataValue> targetType) {
    	return setRenderer(c.getComponentPanel(), targetType, false);
	}
    
    public static boolean setRenderer(DialogComponentColumnFilter c, Class<? extends DataValue> targetType) {
        return setRenderer(c.getComponentPanel(), targetType, true);
    }
    
    public static boolean setRenderer(JComponent c, Class<? extends DataValue> targetType) {
        return setRenderer(c, targetType, false);
    }
    
	@SuppressWarnings("unchecked")
    public static boolean setRenderer(JComponent c, Class<? extends DataValue> targetType, boolean all) {
		if (c instanceof JComboBox) {
			((JComboBox)c).setRenderer(new ConverterDataColumnSpecListCellRenderer(targetType));
			return true;
		}
		if (c instanceof JList) {
            ((JList)c).setCellRenderer(new ConverterDataColumnSpecListCellRenderer(targetType));
            return true;
        }
		for (Component jc : c.getComponents()) {
			if (setRenderer((JComponent)jc, targetType, all)) {
			    if (!all) {
			        return true;
			    }
			}
		}		
		return false;
	}

}