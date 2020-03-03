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
package org.erlwood.knime.nodes.pbfcalc;

import jp.co.infocom.cheminfo.marvin.type.MrvValue;

import org.erlwood.knime.datatypes.converters.MoleculeDataTypeConverter;
import org.erlwood.knime.utils.gui.ConverterDataColumnSpecListCellRenderer;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;

/**
 * <code>NodeDialog</code> for the "PBFcalc" Node. Node to locally calculate PBF
 * values
 * 
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Roger Robinson
 */
public class PBFcalcNodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring the PBFcalc node.
     */
    protected PBFcalcNodeDialog() {

        // Setup Column Selector for Input
        DialogComponentColumnFilter cf;
        cf = new DialogComponentColumnFilter(new SettingsModelFilterString("incExCols"), 0, false,
                MoleculeDataTypeConverter.getColumnFilter(MrvValue.class));

        ConverterDataColumnSpecListCellRenderer.setRenderer(cf, MrvValue.class);
        
        createNewGroup("Columns to PBF value");
        addDialogComponent(cf);
        closeCurrentGroup();

        // Select to remove hydrogens before Calculation
        DialogComponentBoolean remHydrogens;
        remHydrogens = new DialogComponentBoolean(new SettingsModelBoolean("rem_H", true),
                "Remove Hydrogens before calculation");
        addDialogComponent(remHydrogens);

    }
}
