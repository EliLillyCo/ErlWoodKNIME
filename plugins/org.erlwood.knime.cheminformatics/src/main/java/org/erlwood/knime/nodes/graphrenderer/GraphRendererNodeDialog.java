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
package org.erlwood.knime.nodes.graphrenderer;

import org.erlwood.knime.datatypes.converters.MoleculeDataTypeConverter;
import org.erlwood.knime.utils.gui.ConverterDataColumnSpecListCellRenderer;
import org.knime.chem.types.SmilesValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * <code>NodeDialog</code> for the "GraphRenderer" Node.
 * A graph renderer using prefuse java2d library
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Swanand Gore
 */
@SuppressWarnings("all")
public class GraphRendererNodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring GraphRenderer node dialog.
     * This is just a suggestion to demonstrate possible default dialog
     * components.
     */

    protected GraphRendererNodeDialog() {
        super();
        
        addDialogComponent( new DialogComponentColumnNameSelection(
				new SettingsModelString(GraphRendererNodeModel.CFG_PROPCOL, GraphRendererNodeModel.DEFAULT_PROPCOL),
	    		"PropertyColumn", 0, org.knime.core.data.DoubleValue.class
			)
		);
        addDialogComponent( new DialogComponentColumnNameSelection(
				new SettingsModelString(GraphRendererNodeModel.CFG_NAMECOL, GraphRendererNodeModel.DEFAULT_NAMECOL),
	    		"MolNameColumn", 0, org.knime.core.data.StringValue.class
			)
		);
        
        DialogComponentColumnNameSelection molComponent = new DialogComponentColumnNameSelection(
				new SettingsModelString(GraphRendererNodeModel.CFG_SMILESCOL, GraphRendererNodeModel.DEFAULT_SMILESCOL),
	    		"SmilesColumn", 0, MoleculeDataTypeConverter.getColumnFilter(SmilesValue.class));
        
        ConverterDataColumnSpecListCellRenderer.setRenderer(molComponent, SmilesValue.class);
        
        addDialogComponent(molComponent);
        
        addDialogComponent( new DialogComponentNumber(
				new SettingsModelDouble(GraphRendererNodeModel.CFG_EDGECUT, GraphRendererNodeModel.DEFAULT_EDGECUT), "EdgeCutoff", 0.1)
		);
        addDialogComponent( new DialogComponentNumber(
				new SettingsModelDouble(GraphRendererNodeModel.CFG_CLIFFCUT, GraphRendererNodeModel.DEFAULT_CLIFFCUT), "CliffCutoff", 10)
		);
                    
    }
}

